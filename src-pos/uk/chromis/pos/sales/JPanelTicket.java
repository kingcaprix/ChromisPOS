//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) 2015 
//    http://www.chromis.co.uk
//
//    This file is part of Chromis POS
//
//     Chromis POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Chromis POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>.
package uk.chromis.pos.sales;

import bsh.EvalError;
import bsh.Interpreter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import static java.lang.Integer.parseInt;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import uk.chromis.basic.BasicException;
import uk.chromis.data.gui.ComboBoxValModel;
import uk.chromis.data.gui.ListKeyed;
import uk.chromis.data.gui.MessageInf;
import uk.chromis.data.loader.SentenceList;
import uk.chromis.pos.customers.CustomerInfoExt;
import uk.chromis.pos.customers.DataLogicCustomers;
import uk.chromis.pos.customers.JCustomerFinder;
import uk.chromis.pos.forms.AppConfig;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.BeanFactoryApp;
import uk.chromis.pos.forms.BeanFactoryException;
import uk.chromis.pos.forms.DataLogicSales;
import uk.chromis.pos.forms.DataLogicSystem;
import uk.chromis.pos.forms.JPanelView;
import uk.chromis.pos.forms.JRootApp;
import uk.chromis.pos.inventory.TaxCategoryInfo;
import uk.chromis.pos.panels.JProductFinder;
import uk.chromis.pos.payment.JPaymentSelect;
import uk.chromis.pos.payment.JPaymentSelectReceipt;
import uk.chromis.pos.payment.JPaymentSelectRefund;
import uk.chromis.pos.printer.TicketParser;
import uk.chromis.pos.printer.TicketPrinterException;
import uk.chromis.pos.sales.restaurant.RestaurantDBUtils;
import uk.chromis.pos.scale.ScaleException;
import uk.chromis.pos.scripting.ScriptEngine;
import uk.chromis.pos.scripting.ScriptException;
import uk.chromis.pos.scripting.ScriptFactory;
import uk.chromis.pos.ticket.ProductInfoExt;
import uk.chromis.pos.ticket.TaxInfo;
import uk.chromis.pos.ticket.TicketInfo;
import uk.chromis.pos.ticket.TicketLineInfo;
import uk.chromis.pos.ticket.TicketTaxInfo;
import uk.chromis.pos.util.AltEncrypter;
import uk.chromis.pos.util.JRPrinterAWT300;
import uk.chromis.pos.util.ReportUtils;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import uk.chromis.pos.printer.DeviceDisplayAdvance;
import uk.chromis.pos.printer.DeviceTicket;
import uk.chromis.pos.ticket.TicketType;
import uk.chromis.pos.util.AutoLogoff;

/**
 *
 * @author adrianromero
 */
public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor {

    // Variable numerica
    private final static int NUMBERZERO = 0;
    private final static int NUMBERVALID = 1;
    private final static int NUMBER_INPUTZERO = 0;
    private final static int NUMBER_INPUTZERODEC = 1;
    private final static int NUMBER_INPUTINT = 2;
    private final static int NUMBER_INPUTDEC = 3;
    private final static int NUMBER_PORZERO = 4;
    private final static int NUMBER_PORZERODEC = 5;
    private final static int NUMBER_PORINT = 6;
    private final static int NUMBER_PORDEC = 7;

    public static Boolean autoLogoffEnabled;
    public static Boolean autoLogoffInactivity;
    public static Boolean autoLogoffAfterSales;
    public static Boolean autoLogoffToTables;
    public static Boolean autoLogoffAfterKitchen;

    private final String m_sCurrentTicket = null;
    private final String temp_jPrice = "";
    protected JTicketLines m_ticketlines, m_ticketlines2;
    protected TicketInfo m_oTicket;
    protected JPanelButtons m_jbtnconfig;
    protected AppView m_App;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;
    protected Object m_oTicketExt;
    protected TicketsEditor m_panelticket;
    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusPor;
    private TicketParser m_TTP;
    private StringBuffer m_sBarcode;
    private JTicketsBag m_ticketsbag;
    private SentenceList senttax;
    private ListKeyed taxcollection;
    private SentenceList senttaxcategories;
    private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    private TaxesLogic taxeslogic;
    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;
    private JRootApp root;
    private Object m_principalapp;
    private Boolean restaurant;
    private Action logout;
    private Integer delay = 0;
    private DataLogicReceipts dlReceipts = null;
    private Boolean priceWith00;
    private String tableDetails;
    private RestaurantDBUtils restDB;
    private KitchenDisplay kitchenDisplay;
    private String ticketPrintType;
    private Boolean warrantyPrint = false;
    private TicketInfo m_ticket;
    private TicketInfo m_ticketCopy;

    public JPanelTicket() {
        initComponents();
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        restDB = new RestaurantDBUtils(m_App);

        dlSystem = (DataLogicSystem) m_App.getBean("uk.chromis.pos.forms.DataLogicSystem");
        dlSales = (DataLogicSales) m_App.getBean("uk.chromis.pos.forms.DataLogicSales");
        dlCustomers = (DataLogicCustomers) m_App.getBean("uk.chromis.pos.customers.DataLogicCustomers");
        dlReceipts = (DataLogicReceipts) app.getBean("uk.chromis.pos.sales.DataLogicReceipts");

        if (!m_App.getDeviceScale().existsScale()) {
            m_jbtnScale.setVisible(false);
        }
        if (AppConfig.getInstance().getBoolean("till.amountattop")) {
            m_jPanEntries.remove(jPanel9);
            m_jPanEntries.remove(m_jNumberKey);
            m_jPanEntries.add(jPanel9);
            m_jPanEntries.add(m_jNumberKey);
        }

        jbtnMooring.setVisible(AppConfig.getInstance().getBoolean("till.marineoption"));
        priceWith00 = ("true".equals(AppConfig.getInstance().getProperty("till.pricewith00")));
        if (priceWith00) {
            m_jNumberKey.dotIs00(true);
        }

        m_ticketsbag = getJTicketsBag();

        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");
        m_ticketlines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"));
        m_jPanelCentral.add(m_ticketlines, java.awt.BorderLayout.CENTER);

        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);

        m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
        m_jButtonsExt.add(m_jbtnconfig);

        catcontainer.add(getSouthComponent(), BorderLayout.CENTER);

        senttax = dlSales.getTaxList();
        senttaxcategories = dlSales.getTaxCategoriesList();

        taxcategoriesmodel = new ComboBoxValModel();

        stateToZero();

        m_oTicket = null;
        m_oTicketExt = null;

// get all the logoff flags        
        autoLogoffEnabled = AppConfig.getInstance().getBoolean("till.enableautologoff");
        autoLogoffInactivity = AppConfig.getInstance().getBoolean("till.autologoffinactivitytimer");
        autoLogoffAfterSales = AppConfig.getInstance().getBoolean("till.autologoffaftersale");
        autoLogoffToTables = AppConfig.getInstance().getBoolean("till.autologofftotables");
        autoLogoffAfterKitchen = AppConfig.getInstance().getBoolean("till.autologoffafterkitchen");

        /*
        Code to drive full screen display
         */
        if (AppConfig.getInstance().getBoolean("machine.customerdisplay")) {
            if ((app.getDeviceTicket().getDeviceDisplay() != null)
                    && (app.getDeviceTicket().getDeviceDisplay() instanceof DeviceDisplayAdvance)) {
                DeviceDisplayAdvance advDisplay = (DeviceDisplayAdvance) m_App.getDeviceTicket().getDeviceDisplay();
                if (advDisplay.hasFeature(DeviceDisplayAdvance.TICKETLINES)) {
                    m_ticketlines2 = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"));
                    advDisplay.setTicketLines(m_ticketlines2);
                }
                m_ticketlines.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        DeviceDisplayAdvance advDisplay = (DeviceDisplayAdvance) m_App.getDeviceTicket().getDeviceDisplay();

                        if (advDisplay.hasFeature(DeviceDisplayAdvance.PRODUCT_IMAGE)) {
                            if (!e.getValueIsAdjusting()) {
                                int i = m_ticketlines.getSelectedIndex();
                                if (i >= 0) {
                                    try {
                                        String sProduct = m_oTicket.getLine(i).getProductID();
                                        if (sProduct != null) {
                                            ProductInfoExt myProd = JPanelTicket.this.dlSales.getProductInfo(sProduct);
                                            if (myProd == null) {
                                                Logger.getLogger(JPanelTicket.class.getName()).log(Level.INFO, "-------- Null Product pointer(nothing retrieved for " + sProduct + ", check STOCKCURRENT table)");
                                            } else if (myProd.getImage() != null) {
                                                advDisplay.setProductImage(myProd.getImage());
                                            } else {
                                                advDisplay.setProductImage(null);
                                            }
                                        }
                                    } catch (BasicException ex) {
                                        Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                        if (advDisplay.hasFeature(DeviceDisplayAdvance.TICKETLINES)) {
                            int i = m_ticketlines.getSelectedIndex();
                            m_ticketlines2.clearTicketLines();
                            for (int j = 0; (m_oTicket != null) && (j < m_oTicket.getLinesCount()); j++) {
                                m_ticketlines2.insertTicketLine(j, m_oTicket.getLine(j));
                            }
                            m_ticketlines2.setSelectedIndex(i);
                        }
                    }
                });
            }

            /*
        End of screen display
             */
        }
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    private class logout extends AbstractAction {

        public logout() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            System.out.println("In logoff action");
            switch (AppConfig.getInstance().getProperty("machine.ticketsbag")) {
                case "restaurant":
                    if (!autoLogoffToTables && autoLogoffEnabled) {
                        deactivate();
                        ((JRootApp) m_App).closeAppView();
                        break;
                    }
                    deactivate();
                    setActiveTicket(null, null);
                    break;
                default:
                    deactivate();
                    ((JRootApp) m_App).closeAppView();
            }
        }
    }

    private void saveCurrentTicket() {
        String currentTicket = (String) m_oTicketExt;
        if (currentTicket != null) {
            try {
                dlReceipts.updateSharedTicket(currentTicket, m_oTicket, m_oTicket.getPickupId());
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }
    }

    @Override
    public void activate() throws BasicException {
// if the autologoff and inactivity is configured the setup the timer with action
        Action logout = new logout();
        if (autoLogoffEnabled && autoLogoffInactivity) {
            try {
                delay = Integer.parseInt(AppConfig.getInstance().getProperty("till.autologofftimerperiod"));
                if (delay != 0) {
                    AutoLogoff.getInstance().setTimer(delay * 1000, logout);
                }
            } catch (NumberFormatException e) {
                delay = 0;
            }
        }

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this);
        paymentdialogrefund.init(m_App);

        // impuestos incluidos seleccionado ?
        m_jaddtax.setSelected("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

        java.util.List<TaxInfo> taxlist = senttax.list();
        taxcollection = new ListKeyed<>(taxlist);
        java.util.List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();
        taxcategoriescollection = new ListKeyed<>(taxcategorieslist);

        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        m_jTax.setModel(taxcategoriesmodel);

        String taxesid = m_jbtnconfig.getProperty("taxcategoryid");
        if (taxesid == null) {
            if (m_jTax.getItemCount() > 0) {
                m_jTax.setSelectedIndex(0);
            }
        } else {
            taxcategoriesmodel.setSelectedKey(taxesid);
        }

        taxeslogic = new TaxesLogic(taxlist);

        m_jaddtax.setSelected((Boolean.parseBoolean(AppConfig.getInstance().getProperty("till.taxincluded"))));

        // Show taxes options
        if (m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions")) {
            m_jTax.setVisible(true);
            m_jaddtax.setVisible(true);
        } else {
            m_jTax.setVisible(false);
            m_jaddtax.setVisible(false);
        }

        // Authorization for buttons
        btnSplit.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jDelete.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        //     m_jNumberKey.setMinusEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKey.setEqualsEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());

        m_ticketsbag.activate();

    }

    @Override
    public boolean deactivate() {
        AutoLogoff.getInstance().deactivateTimer();
        //Listener.stop();
        return m_ticketsbag.deactivate();
    }

    protected abstract JTicketsBag getJTicketsBag();

    protected abstract Component getSouthComponent();

    protected abstract void resetSouthComponent();

    protected abstract void reLoadCatalog();

    @SuppressWarnings("empty-statement")
    @Override
    public void setActiveTicket(TicketInfo oTicket, Object oTicketExt) {
// check if a inactivity timer has been created, and if it is not running start up again
// this is required for autologoff mode in restaurant and it is set to return to the table view.        
        switch (AppConfig.getInstance().getProperty("machine.ticketsbag")) {
            case "restaurant":
                if (autoLogoffEnabled && autoLogoffInactivity) {
                    System.out.println("setActive ticket");
                    if (!AutoLogoff.getInstance().isTimerRunning()) {
                        AutoLogoff.getInstance().activateTimer();
                    }
                }
        }

        m_oTicket = oTicket;
        m_oTicketExt = oTicketExt;

        if (m_oTicket != null) {
            // Asign preliminary properties to the receipt
            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            m_oTicket.setActiveCash(m_App.getActiveCashIndex());
            m_oTicket.setDate(new Date()); // Set the edition date.

// Set some of the table details here if in restaurant mode
            if ("restaurant".equals(AppConfig.getInstance().getProperty("machine.ticketsbag")) && !oTicket.getOldTicket()) {
// Check if there is a customer name in the database for this table

                if (restDB.getCustomerNameInTable(oTicketExt.toString()) == null) {
                    if (m_oTicket.getCustomer() != null) {
                        restDB.setCustomerNameInTable(m_oTicket.getCustomer().toString(), oTicketExt.toString());
                    }
                }
//Check if the waiters name is in the table, this will be the person who opened the ticket                        
                if (restDB.getWaiterNameInTable(oTicketExt.toString()) == null || "".equals(restDB.getWaiterNameInTable(oTicketExt.toString()))) {
                    restDB.setWaiterNameInTable(m_App.getAppUserView().getUser().getName(), oTicketExt.toString());
                }
                restDB.setTicketIdInTable(m_oTicket.getId(), oTicketExt.toString());

            }
        }

// lets check if this is a moved ticket        
        if ((m_oTicket != null) && (((Boolean.parseBoolean(AppConfig.getInstance().getProperty("table.showwaiterdetails")))
                || (AppConfig.getInstance().getBoolean("table.showcustomerdetails"))))) {

        }
        if ((m_oTicket != null) && (((AppConfig.getInstance().getBoolean("table.showcustomerdetails"))
                || (AppConfig.getInstance().getBoolean("table.showwaiterdetails"))))) {
// check if the old table and the new table are the same                      
            if (restDB.getTableMovedFlag(m_oTicket.getId())) {
                restDB.moveCustomer(oTicketExt.toString(), m_oTicket.getId());
            }
        }
        // read resources ticket.show and execute
        executeEvent(m_oTicket, m_oTicketExt, "ticket.show");
        j_btnKitchenPrt.setVisible(m_App.getAppUserView().getUser().hasPermission("sales.PrintKitchen"));
        refreshTicket();
    }

    @Override
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }

    private void refreshTicket() {

        CardLayout cl = (CardLayout) (getLayout());

        if (m_oTicket == null) {
            m_jTicketId.setText(null);
            m_ticketlines.clearTicketLines();

            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);

            stateToZero();
            repaint();

            // Muestro el panel de nulos.
            cl.show(this, "null");

            if ((m_oTicket != null) && (m_oTicket.getLinesCount() == 0)) {
                resetSouthComponent();
            }

        } else {
            if (m_oTicket.getTicketType().equals(TicketType.REFUND)) {
                //Make disable Search and Edit Buttons
                m_jNumberKey.justEquals();
                jEditAttributes.setVisible(false);
                m_jEditLine.setVisible(false);
                m_jList.setVisible(false);
            }

            // Refresh ticket taxes
            for (TicketLineInfo line : m_oTicket.getLines()) {
                line.setTaxInfo(taxeslogic.getTaxInfo(line.getProductTaxCategoryID(), m_oTicket.getCustomer()));
            }

            // The ticket name
            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));

            // Limpiamos todas las filas y anadimos las del ticket actual
            m_ticketlines.clearTicketLines();

            for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                m_ticketlines.addTicketLine(m_oTicket.getLine(i));
            }
            printPartialTotals();
            stateToZero();

            // Muestro el panel de tickets.
            cl.show(this, "ticket");
            if (m_oTicket.getLinesCount() == 0) {
                resetSouthComponent();
            }

            // activo el tecleador...
            m_jKeyFactory.setText(null);
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    m_jKeyFactory.requestFocus();
                }
            });
        }
    }

    private void printPartialTotals() {
        if (m_oTicket.getLinesCount() == 0) {
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);
            repaint();
        } else {
            m_jSubtotalEuros.setText(m_oTicket.printSubTotal());
            m_jTaxesEuros.setText(m_oTicket.printTax());
            m_jTotalEuros.setText(m_oTicket.printTotal());
        }
    }

    private void paintTicketLine(int index, TicketLineInfo oLine) {
        if (executeEventAndRefresh("ticket.setline", new ScriptArg("index", index), new ScriptArg("line", oLine)) == null) {
            m_oTicket.setLine(index, oLine);
            m_ticketlines.setTicketLine(index, oLine);
            m_ticketlines.setSelectedIndex(index);

            visorTicketLine(oLine);
            printPartialTotals();
            stateToZero();
            executeEventAndRefresh("ticket.change");
        }
    }

    private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {
        if (oProduct.isVprice()) {
            TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
            if (m_jaddtax.isSelected()) {
                dPrice /= (1 + tax.getRate());
            }
            addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone())));
        } else {
            TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
            addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone())));
        }
    }

    protected void addTicketLine(TicketLineInfo oLine) {
        // read resource ticket.addline and exececute
        if (executeEventAndRefresh("ticket.addline", new ScriptArg("line", oLine)) == null) {
            if (oLine.isProductCom()) {
                // Comentario entonces donde se pueda
                int i = m_ticketlines.getSelectedIndex();
                // me salto el primer producto normal...
                if (i >= 0 && !m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }
                // me salto todos los productos auxiliares...                
                while (i >= 0 && i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }
                if (i >= 0) {
                    m_oTicket.insertLine(i, oLine);
                    m_ticketlines.insertTicketLine(i, oLine); // Pintamos la linea en la vista...                 
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } else {
                m_oTicket.addLine(oLine);
                m_ticketlines.addTicketLine(oLine); // Pintamos la linea en la vista... 

                try {
                    int i = m_ticketlines.getSelectedIndex();
                    TicketLineInfo line = m_oTicket.getLine(i);
                    if (line.isProductVerpatrib()) {
                        JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                        attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                        attedit.setVisible(true);
                        if (attedit.isOK()) {
                            line.setProductAttSetInstId(attedit.getAttributeSetInst());
                            line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                            paintTicketLine(i, line);
                        }
                    }
                } catch (BasicException ex) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                    msg.show(this);
                }
            }

            if (Boolean.parseBoolean(AppConfig.getInstance().getProperty("display.consolidated"))) {
// includes modified consolidate receipt code for screen and refreshes the screen afer updating     
                int numlines = m_oTicket.getLinesCount();
                for (int i = 0; i < numlines; i++) {
                    TicketLineInfo current_ticketline = m_oTicket.getLine(i);
                    double current_unit = current_ticketline.getMultiply();
                    if (current_unit != 0.0D) {
                        for (int j = i + 1; j < numlines; j++) {
                            if ((m_oTicket.getLine(j).getProductID() != null) && (m_oTicket.getLine(j).getProductName() != "")) {
                                TicketLineInfo loop_ticketline = m_oTicket.getLine(j);
                                double loop_unit = loop_ticketline.getMultiply();
                                String current_productid = current_ticketline.getProductID();
                                String loop_productid = loop_ticketline.getProductID();
                                String loop_attr = loop_ticketline.getProductAttSetInstDesc();
                                String current_attr = current_ticketline.getProductAttSetInstDesc();
                                String current_name = current_ticketline.getProductName();
                                String loop_name = loop_ticketline.getProductName();

                                if (Boolean.parseBoolean(AppConfig.getInstance().getProperty("display.consolidatedwithoutprice"))) {
                                    if ((loop_productid.equals(current_productid)) && (loop_unit != 0.0D) && (loop_attr.equals(current_attr)) && (loop_name.equals(current_name))) {
                                        current_unit += loop_unit;
                                        loop_ticketline.setMultiply(0.0D);
                                    }
                                } else if ((loop_productid.equals(current_productid)) && (loop_ticketline.getPrice() == current_ticketline.getPrice()) && (loop_unit != 0.0D) && (loop_attr.equals(current_attr)) && (loop_name.equals(current_name))) {
                                    current_unit += loop_unit;
                                    loop_ticketline.setMultiply(0.0D);
                                }
                            }
                        }
                        current_ticketline.setMultiply(current_unit);
                    }
                }
                for (int i = numlines - 1; i > 0; i--) {
                    TicketLineInfo loop_ticketline = m_oTicket.getLine(i);
                    double loop_unit = loop_ticketline.getMultiply();
                    if (loop_unit == 0) {
                        m_oTicket.removeLine(i);

                    }
                }
                refreshTicket();
            }

            executeEventAndRefresh("ticket.pretotals");

            visorTicketLine(oLine);
            printPartialTotals();
            stateToZero();

            // read resource ticket.change and execute
            executeEvent(m_oTicket, m_oTicketExt, "ticket.change");
        }

    }

    private void removeTicketLine(int i) {

        if (("OK".equals(m_oTicket.getLine(i).getProperty("sendstatus")) && m_App.getAppUserView().getUser().hasPermission("kitchen.DeleteLine"))
                || (!"OK".equals(m_oTicket.getLine(i).getProperty("sendstatus")) && m_App.getAppUserView().getUser().hasPermission("sales.EditLines"))) {
            //read resource ticket.removeline and execute
            if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {

                String ticketID = Integer.toString(m_oTicket.getTicketId());
                if (m_oTicket.getTicketId() == 0) {
                    ticketID = "No Sale";
                }

                dlSystem.execLineRemoved(
                        new Object[]{
                            m_App.getAppUserView().getUser().getName(),
                            ticketID,
                            m_oTicket.getLine(i).getProductID(),
                            m_oTicket.getLine(i).getProductName(),
                            m_oTicket.getLine(i).getMultiply()
                        });

                if (m_oTicket.getLine(i).isProductCom()) {
                    m_oTicket.removeLine(i);
                    m_ticketlines.removeTicketLine(i);
                } else {
                    m_oTicket.removeLine(i);
                    m_ticketlines.removeTicketLine(i);
                    while (i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                        m_oTicket.removeLine(i);
                        m_ticketlines.removeTicketLine(i);
                    }
                }

                visorTicketLine(null);
                printPartialTotals();
                stateToZero();

                executeEventAndRefresh("ticket.change");
            }
        } else {
            JOptionPane.showMessageDialog(this, AppLocal.getIntString("message.cannotdeletesentline"), "Notice", JOptionPane.INFORMATION_MESSAGE);

        }

    }

    private ProductInfoExt getInputProduct() {
        ProductInfoExt oProduct = new ProductInfoExt();
        oProduct.setID("xxx999_999xxx_x9x9x9");
        oProduct.setReference(null);
        oProduct.setCode(null);
        oProduct.setName("");
        oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
        oProduct.setPriceSell(includeTaxes(oProduct.getTaxCategoryID(), getInputValue()));
        return oProduct;
    }

    private double includeTaxes(String tcid, double dValue) {
        if (m_jaddtax.isSelected()) {
            TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
            double dTaxRate = tax == null ? 0.0 : tax.getRate();
            return dValue / (1.0 + dTaxRate);
        } else {
            return dValue;
        }
    }

    private double excludeTaxes(String tcid, double dValue) {
        TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
        double dTaxRate = tax == null ? 0.0 : tax.getRate();
        return dValue / (1.0 + dTaxRate);
    }

    private double getInputValue() {
        try {
            // Double ret = Double.parseDouble(m_jPrice.getText());
            // return priceWith00 ? ret / 100 : ret;
            return Double.parseDouble(m_jPrice.getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double getPorValue() {
        try {
            return Double.parseDouble(m_jPor.getText().substring(1));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 1.0;
        }
    }

    private void stateToZero() {
        m_jPor.setText("");
        m_jPrice.setText("");
        m_sBarcode = new StringBuffer();
        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusPor = NUMBERZERO;
        repaint();
    }

    private void incProductByCode(String sCode) {
        // precondicion: sCode != null

// Modify to allow number x with scanned products. JDL 8.8.2015        
        int count = 1;
        if (sCode.contains("*")) {
            count = (sCode.indexOf("*") == 0) ? 1 : parseInt(sCode.substring(0, sCode.indexOf("*")));
            sCode = sCode.substring(sCode.indexOf("*") + 1, sCode.length());
        }
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null,
                        sCode + " - " + AppLocal.getIntString("message.noproduct"), "Check", JOptionPane.WARNING_MESSAGE);
                stateToZero();
            } else {
                incProduct(count, oProduct);
            }
        } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);
        }
    }

    private void incProductByCodePrice(String sCode, double dPriceSell) {
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {
                Toolkit.getDefaultToolkit().beep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);
                stateToZero();
            } else if (m_jaddtax.isSelected()) {
                TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
                addTicketLine(oProduct, 1.0, dPriceSell / (1.0 + tax.getRate()));
            } else {
                addTicketLine(oProduct, 1.0, dPriceSell);
            }
        } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);
        }
    }

    private void incProduct(ProductInfoExt prod) {
        if (prod.isScale() && m_App.getDeviceScale().existsScale()) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    incProduct(value, prod);
                }
            } catch (ScaleException e) {
                Toolkit.getDefaultToolkit().beep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                stateToZero();
            }
        } else if (!prod.isVprice()) {
            incProduct(1.0, prod);
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    AppLocal.getIntString("message.novprice"));
        }
    }

    private void incProduct(double dPor, ProductInfoExt prod) {
        if (prod.isVprice()) {
            addTicketLine(prod, getPorValue(), getInputValue());
        } else {
            addTicketLine(prod, dPor, prod.getPriceSell());
        }

    }

    protected void buttonTransition(ProductInfoExt prod) {
        if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
            incProduct(prod);
        } else if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
            incProduct(getInputValue(), prod);
        } else if (prod.isVprice()) {
            addTicketLine(prod, getPorValue(), getInputValue());
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void stateTransition(char cTrans) {
        if ((cTrans == '\n') || (cTrans == '?')) {

            /**
             * ******************************************************************
             * Start of barcode handling routine
             * ******************************************************************
             */
            if (m_sBarcode.length() > 0) {
                String sCode = m_sBarcode.toString();
                /**
                 * *****************************************************************************
                 * Kidsgrove Tropicals voucher code
                 * ******************************************************************************
                 */
                if (sCode.startsWith("05V")) {
// £5.00 voucher                           
                    try {
                        if (dlSales.getVoucher(sCode)) {
                            stateToZero();
                            JOptionPane.showMessageDialog(null, "Voucher Code \"" + sCode + "\" already Sold. Please use another voucher", "Invalid Vocuher", JOptionPane.WARNING_MESSAGE);
                        } else if (checkVoucherCurrentTicket(sCode)) {
                            stateToZero();
                            JOptionPane.showMessageDialog(null, "Voucher Code \"" + sCode + "\" already on Ticket. Please use another voucher", "Invalid Vocuher", JOptionPane.WARNING_MESSAGE);
                        } else {
                            ProductInfoExt oProduct = new ProductInfoExt();
                            oProduct = dlSales.getProductInfoByCode("05V");
                            if (oProduct != null) {
                                oProduct.setCode("05V");
                                oProduct.setName(oProduct.getName() + sCode);
                                oProduct.setProperty("vCode", sCode);
                                oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                                addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(null,
                                        "Vocher code 05V - " + AppLocal.getIntString("message.noproduct"),
                                        "Check", JOptionPane.WARNING_MESSAGE);
                                stateToZero();
                            }
                        }
                    } catch (BasicException ex) {
                        Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (sCode.startsWith("10V")) {
// £10.00 voucher   
                    try {
                        if (dlSales.getVoucher(sCode)) {
                            stateToZero();
                            JOptionPane.showMessageDialog(null, "Voucher Code \"" + sCode + "\" already Sold. Please use another voucher", "Invalid Vocuher", JOptionPane.WARNING_MESSAGE);
                        } else if (checkVoucherCurrentTicket(sCode)) {
                            stateToZero();
                            JOptionPane.showMessageDialog(null, "Voucher Code \"" + sCode + "\" already on Ticket. Please use another voucher", "Invalid Vocuher", JOptionPane.WARNING_MESSAGE);
                        } else {
                            ProductInfoExt oProduct = new ProductInfoExt();
                            oProduct = dlSales.getProductInfoByCode("10V");
                            if (oProduct != null) {
                                oProduct.setCode("10V");
                                oProduct.setName(oProduct.getName() + sCode);
                                oProduct.setProperty("vCode", sCode);
                                oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                                addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(null,
                                        "Vocher code 10V - " + AppLocal.getIntString("message.noproduct"),
                                        "Check", JOptionPane.WARNING_MESSAGE);
                                stateToZero();
                            }
                        }
                    } catch (BasicException ex) {
                        Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (sCode.startsWith("20V")) {
// £20.00 voucher     
                    try {
                        if (dlSales.getVoucher(sCode)) {
                            stateToZero();
                            JOptionPane.showMessageDialog(null, "Voucher Code \"" + sCode + "\" already Sold. Please use another voucher", "Invalid Vocuher", JOptionPane.WARNING_MESSAGE);
                        } else if (checkVoucherCurrentTicket(sCode)) {
                            stateToZero();
                            JOptionPane.showMessageDialog(null, "Voucher Code \"" + sCode + "\" already on Ticket. Please use another voucher", "Invalid Vocuher", JOptionPane.WARNING_MESSAGE);
                        } else {
                            ProductInfoExt oProduct = new ProductInfoExt();
                            oProduct = dlSales.getProductInfoByCode("20V");
                            if (oProduct != null) {
                                oProduct.setProperty("vCode", sCode);
                                oProduct.setCode("20V");
                                oProduct.setName(oProduct.getName() + sCode);
                                oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                                addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(null,
                                        "Voucher code 20V - " + AppLocal.getIntString("message.noproduct"),
                                        "Check", JOptionPane.WARNING_MESSAGE);
                                stateToZero();
                            }
                        }
                    } catch (BasicException ex) {
                        Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    /**
                     * *****************************************************************************
                     * End Kidsgrove Tropicals voucher codes
                     * ******************************************************************************
                     */
// Are we passing a customer card these cards start with 'c'                
                } else if (sCode.startsWith("c")) {
                    try {
                        CustomerInfoExt newcustomer = dlSales.findCustomerExt(sCode);
                        if (newcustomer == null) {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(null,
                                    sCode + " - " + AppLocal.getIntString("message.nocustomer"),
                                    "Customer Not Found", JOptionPane.WARNING_MESSAGE);
                        } else {
                            m_oTicket.setCustomer(newcustomer);
                            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
                        }
                    } catch (BasicException e) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(null,
                                sCode + " - " + AppLocal.getIntString("message.nocustomer"),
                                "Customer Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                    stateToZero();
// lets look at variable price barcodes thhat conform to GS1 standard
// For more details see Chromis docs
                } else if (((sCode.length() == 13) && (sCode.startsWith("2"))) || ((sCode.length() == 12) && (sCode.startsWith("02")))) {
// we now have a variable barcode being passed   
// get the variable type   
                    ProductInfoExt oProduct = null;
                    try {
                        oProduct = dlSales.getProductInfoByCode(sCode);
                    } catch (BasicException ex) {
                        Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // no exact match for the product
                    if (oProduct != null) {
                        incProductByCode(sCode);
                    } else {

                        String sVariableTypePrefix;
                        String prodCode;
                        String sVariableNum;
                        double dPriceSell = 0.0;
                        double weight = 1.0;

                        if (sCode.length() == 13) {
                            sVariableTypePrefix = sCode.substring(0, 2);
                            sVariableNum = sCode.substring(8, 12);
                            prodCode = sCode.replace(sCode.substring(7, sCode.length() - 1), "00000");
                            prodCode = prodCode.substring(0, sCode.length() - 1);
                        } else {
                            sVariableTypePrefix = sCode.substring(0, 2);;
                            sVariableNum = sCode.substring(7, 11);
                            prodCode = sCode.replace(sCode.substring(6, sCode.length() - 1), "00000");
                            prodCode = prodCode.substring(0, sCode.length() - 1);
                        }
                        if (sCode.length() == 13) {
                            switch (sVariableTypePrefix) {
                                case "20":
                                    dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                    break;
                                case "21":
                                    dPriceSell = Double.parseDouble(sVariableNum) / 10;
                                    break;
                                case "22":
                                    dPriceSell = Double.parseDouble(sVariableNum);
                                    break;
                                case "23":
                                    weight = Double.parseDouble(sVariableNum) / 1000;
                                    break;
                                case "24":
                                    weight = Double.parseDouble(sVariableNum) / 100;
                                    break;
                                case "25":
                                    weight = Double.parseDouble(sVariableNum) / 10;
                                    break;
                                case "28":
                                    sVariableNum = sCode.substring(7, 12);
                                    dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                    break;
                            }
                        } else if (sCode.length() == 12) {
                            switch (sVariableTypePrefix) {
                                case "02":
                                    sVariableNum = sCode.substring(6, 11);
                                    dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                    break;
                            }
                        }
// we now know the product code and the price or weight of it.
// lets check for the product in the database. 
                        try {
                            oProduct = dlSales.getProductInfoByCode(prodCode);
                            if (oProduct == null) {
                                Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(null,
                                        prodCode + " - " + AppLocal.getIntString("message.noproduct"),
                                        "Check", JOptionPane.WARNING_MESSAGE);
                                stateToZero();
                            } else if (sCode.length() == 13) {
                                switch (sVariableTypePrefix) {
                                    case "23":
                                    case "24":
                                    case "25":
                                        oProduct.setProperty("product.weight", Double.toString(weight));
                                        dPriceSell = oProduct.getPriceSell();
                                        break;
                                }
                            } else // Handle UPC code, get the product base price if zero then it is a price passed otherwise it is a weight                                
                             if (oProduct.getPriceSell() != 0.0) {
                                    weight = Double.parseDouble(sVariableNum) / 100;
                                    oProduct.setProperty("product.weight", Double.toString(weight));
                                    dPriceSell = oProduct.getPriceSell();
                                } else {
                                    dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                }
                            if (m_jaddtax.isSelected()) {
                                addTicketLine(oProduct, weight, dPriceSell);
                            } else {
                                TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
                                addTicketLine(oProduct, weight, dPriceSell / (1.0 + tax.getRate()));
                            }
                        } catch (BasicException eData) {
                            stateToZero();
                            new MessageInf(eData).show(this);
                        }
                    }
                } else {
                    incProductByCode(sCode);
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }

            /**
             * ******************************************************************
             * end of barcode handling routine
             * ******************************************************************
             */
        } else {
            m_sBarcode.append(cTrans);
            if (cTrans == '\u007f') {
                stateToZero();
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_INPUTZERO)) {
                m_jPrice.setText("0");
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9') && (m_iNumberStatus == NUMBER_INPUTZERO)) {
                if (!priceWith00) {
                    m_jPrice.setText(Character.toString(cTrans));
                } else {
                    m_jPrice.setText(setTempjPrice(Character.toString(cTrans)));
                }
                m_iNumberStatus = NUMBER_INPUTINT;
                m_iNumberStatusInput = NUMBERVALID;
            } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9') && (m_iNumberStatus == NUMBER_INPUTINT)) {
                if (!priceWith00) {
                    m_jPrice.setText(m_jPrice.getText() + cTrans);
                } else {
                    m_jPrice.setText(setTempjPrice(m_jPrice.getText() + cTrans));
                }
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTZERO && !priceWith00) {
                m_jPrice.setText("0.");
                m_iNumberStatus = NUMBER_INPUTZERODEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTZERO) {
                m_jPrice.setText("");
                m_iNumberStatus = NUMBER_INPUTZERO;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTINT && !priceWith00) {
                m_jPrice.setText(m_jPrice.getText() + ".");
                m_iNumberStatus = NUMBER_INPUTDEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTINT) {
                if (!priceWith00) {
                    m_jPrice.setText(m_jPrice.getText() + "00");
                } else {
                    m_jPrice.setText(setTempjPrice(m_jPrice.getText() + "00"));
                }
                m_iNumberStatus = NUMBER_INPUTINT;
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) {
                if (!priceWith00) {
                    m_jPrice.setText(m_jPrice.getText() + cTrans);
                } else {
                    m_jPrice.setText(setTempjPrice(m_jPrice.getText() + cTrans));
                }
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9') && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) {
                m_jPrice.setText(m_jPrice.getText() + cTrans);
                m_iNumberStatus = NUMBER_INPUTDEC;
                m_iNumberStatusInput = NUMBERVALID;
            } else if (cTrans == '*' && (m_iNumberStatus == NUMBER_INPUTINT || m_iNumberStatus == NUMBER_INPUTDEC)) {
                m_jPor.setText("x");
                m_iNumberStatus = NUMBER_PORZERO;
            } else if (cTrans == '*' && (m_iNumberStatus == NUMBER_INPUTZERO || m_iNumberStatus == NUMBER_INPUTZERODEC)) {
                m_jPrice.setText("0");
                m_jPor.setText("x");
                m_iNumberStatus = NUMBER_PORZERO;
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_PORZERO)) {
                m_jPor.setText("x0");
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9') && (m_iNumberStatus == NUMBER_PORZERO)) {
                m_jPor.setText("x" + Character.toString(cTrans));
                m_iNumberStatus = NUMBER_PORINT;
                m_iNumberStatusPor = NUMBERVALID;
            } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9') && (m_iNumberStatus == NUMBER_PORINT)) {
                m_jPor.setText(m_jPor.getText() + cTrans);
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORZERO && !priceWith00) {
                m_jPor.setText("x0.");
                m_iNumberStatus = NUMBER_PORZERODEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORZERO) {
                m_jPor.setText("x");
                m_iNumberStatus = NUMBERVALID;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORINT && !priceWith00) {
                m_jPor.setText(m_jPor.getText() + ".");
                m_iNumberStatus = NUMBER_PORDEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORINT) {
                m_jPor.setText(m_jPor.getText() + "00");
                m_iNumberStatus = NUMBERVALID;
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) {
                m_jPor.setText(m_jPor.getText() + cTrans);
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                    && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) {
                m_jPor.setText(m_jPor.getText() + cTrans);
                m_iNumberStatus = NUMBER_PORDEC;
                m_iNumberStatusPor = NUMBERVALID;
            } else if (cTrans == '\u00a7'
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                if (m_App.getDeviceScale().existsScale() && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    try {
                        Double value = m_App.getDeviceScale().readWeight();
                        if (value != null) {
                            ProductInfoExt product = getInputProduct();
                            addTicketLine(product, value, product.getPriceSell());
                        }
                    } catch (ScaleException e) {
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                        stateToZero();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } else if (cTrans == '\u00a7'
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    Toolkit.getDefaultToolkit().beep();
                } else if (m_App.getDeviceScale().existsScale()) {
                    try {
                        Double value = m_App.getDeviceScale().readWeight();
                        if (value != null) {
                            TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                            newline.setMultiply(value);
                            newline.setPrice(Math.abs(newline.getPrice()));
                            paintTicketLine(i, newline);
                        }
                    } catch (ScaleException e) {
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                        stateToZero();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    if (m_oTicket.getTicketType().equals(TicketType.REFUND)) {
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        paintTicketLine(i, newline);
                    } else {
                        newline.setMultiply(newline.getMultiply() + 1.0);
                        paintTicketLine(i, newline);
                    }
                }
            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    //If it's a refund - button means one unit more
                    if (m_oTicket.getTicketType().equals(TicketType.REFUND)) {
                        newline.setMultiply(newline.getMultiply() + 1.0);
                        if (newline.getMultiply() >= 0) {
                            removeTicketLine(i);
                        } else {
                            paintTicketLine(i, newline);
                        }
                    } else {
                        // substract one unit to the selected line
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        if (newline.getMultiply() <= 0.0) {
                            removeTicketLine(i); // elimino la linea
                        } else {
                            paintTicketLine(i, newline);
                        }
                    }
                }
            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERVALID) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    double dPor = getPorValue();
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    if (m_oTicket.getTicketType().equals(TicketType.REFUND)) {
                        newline.setMultiply(-dPor);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    } else {
                        newline.setMultiply(dPor);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    }
                }
            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERVALID
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    double dPor = getPorValue();
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    if (m_oTicket.getTicketType().equals(TicketType.NORMAL) || m_oTicket.getTicketType().equals(TicketType.NORMAL)) {
                        newline.setMultiply(dPor);
                        newline.setPrice(-Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    }
                }
            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, product.getPriceSell());

                if (!Boolean.parseBoolean(AppConfig.getInstance().getProperty("product.hidedefaultproductedit"))) {
                    m_jEditLine.doClick();
                }
            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines") && m_sBarcode.length() < 2) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, -product.getPriceSell());
            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERVALID
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), product.getPriceSell());
            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERVALID
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines") && m_sBarcode.length() < 2) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), -product.getPriceSell());
            } else if (cTrans == ' ' || cTrans == '=') {
                if (m_oTicket.getLinesCount() > 0) {
                    if (closeTicket(m_oTicket, m_oTicketExt)) {
                        if (m_oTicket.getTicketType().equals(TicketType.REFUND)) {
                            try {
                                JRefundLines.updateRefunds();
                            } catch (BasicException ex) {
                                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        m_ticketsbag.deleteTicket();
                        if ((!("restaurant".equals(AppConfig.getInstance().getProperty("machine.ticketsbag"))) && autoLogoffEnabled && autoLogoffAfterSales)) {
                            ((JRootApp) m_App).closeAppView();
                        }
                    } else {
                        refreshTicket();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

    private boolean closeTicket(TicketInfo ticket, Object ticketext) {
        AutoLogoff.getInstance().deactivateTimer();
        boolean resultok = false;

        if (m_App.getAppUserView().getUser().hasPermission("sales.Total")) {
// Check if we have a warranty to print                         
            warrantyCheck(ticket);

            try {
                // reset the payment info
                taxeslogic.calculateTaxes(ticket);
                if (ticket.getTotal() >= 0.0) {
                    ticket.resetPayments(); //Only reset if is sale
                }
                //read resource ticket.total and execute
                if (executeEvent(ticket, ticketext, "ticket.total") == null) {
                    // Muestro el total
                    printTicket("Printer.TicketTotal", ticket, ticketext);

                    // Select the Payments information
                    JPaymentSelect paymentdialog = ticket.getTicketType().equals(TicketType.REFUND)
                            ? paymentdialogrefund
                            : paymentdialogreceipt;
                    paymentdialog.setPrintSelected("true".equals(m_jbtnconfig.getProperty("printselected", "true")));

                    paymentdialog.setTransactionID(ticket.getTransactionID());

                    if (paymentdialog.showDialog(ticket.getTotal(), ticket.getCustomer())) {

                        // assign the payments selected and calculate taxes.         
                        ticket.setPayments(paymentdialog.getSelectedPayments());

                        // Asigno los valores definitivos del ticket...
                        ticket.setUser(m_App.getAppUserView().getUser().getUserInfo()); // El usuario que lo cobra
                        ticket.setActiveCash(m_App.getActiveCashIndex());
                        ticket.setDate(new Date()); // Le pongo la fecha de cobro

                        //read resource ticket.save and execute
                        if (executeEvent(ticket, ticketext, "ticket.save") == null) {
                            // Save the receipt and assign a receipt number
                            try {
                                dlSales.saveTicket(ticket, m_App.getInventoryLocation());

// Kidsgrove here the payment has been confirmed lets save voucher details into database vCode10V0061
                                for (TicketLineInfo line : m_oTicket.getLines()) {
                                    if ((line.getProperty("vCode") != "") && (line.getProperty("vCode") != null)) {
                                        try {
                                            dlSales.sellVoucher(
                                                    new Object[]{line.getProperty("vCode"), Integer.toString(ticket.getTicketId())});
                                        } catch (BasicException ex) {
                                        }
                                    }
                                }
                                // code added to allow last ticket reprint       
                                AppConfig.getInstance().setProperty("lastticket.number", Integer.toString(ticket.getTicketId()));
                                AppConfig.getInstance().setProperty("lastticket.type", Integer.toString(ticket.getTicketType().getId())); 
                                AppConfig.getInstance().save();
                            } catch (BasicException eData) {
                                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                                msg.show(this);

                            } catch (IOException ex) {
                                Logger.getLogger(JPanelTicket.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                            //read resource ticket.close and execute
                            executeEvent(ticket, ticketext, "ticket.close", new ScriptArg("print", paymentdialog.isPrintSelected()));

                            printTicket(paymentdialog.isPrintSelected() || warrantyPrint
                                    ? "Printer.Ticket"
                                    : "Printer.Ticket2", ticket, ticketext);

//                            if (m_oTicket.getLoyaltyCardNumber() != null){
// add points to the card
//                                System.out.println("Point added to card = " + ticket.getTotal()/100);
// reset card pointer                                
                            //  loyaltyCardNumber = null;
//                            }
                            resultok = true;
// if restaurant clear any customer name in table for this table once receipt is printed
                            if ("restaurant".equals(AppConfig.getInstance().getProperty("machine.ticketsbag")) && !ticket.getOldTicket()) {
                                restDB.clearCustomerNameInTable(ticketext.toString());
                                restDB.clearWaiterNameInTable(ticketext.toString());
                                restDB.clearTicketIdInTable(ticketext.toString());
                            }
                        }
                    }
                }
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
                resultok = false;
            }

            // reset the payment info
            m_oTicket.resetTaxes();
            m_oTicket.resetPayments();
        }
/* Commented out due to not functioning as expected.
// Reset the customer display here         
        String sresource = dlSystem.getResourceAsXML("Display.Message");
        DeviceTicket m_TP = new DeviceTicket(this, AppConfig.getInstance());
        if (sresource == null) {
            m_TP.getDeviceDisplay().writeVisor(AppLocal.APP_NAME, AppLocal.APP_VERSION);
        } else {
            try {
                m_TTP.printTicket(sresource);
            } catch (TicketPrinterException eTP) {
                m_TP.getDeviceDisplay().writeVisor(AppLocal.APP_NAME, AppLocal.APP_VERSION);
            }
        }
//*/
        // cancelled the ticket.total script
        // or canceled the payment dialog
        // or canceled the ticket.close script
        AutoLogoff.getInstance().activateTimer();
        return resultok;
    }

    private boolean checkVoucherCurrentTicket(String voucher) {
        for (TicketLineInfo line : m_oTicket.getLines()) {
            if (line.getProperty("vCode").equals(voucher)) {
                return (true);
            }
        }
        return (false);
    }

    private boolean warrantyCheck(TicketInfo ticket) {
        warrantyPrint = false;
        int lines = 0;
        while (lines < ticket.getLinesCount()) {
            if (!warrantyPrint) {
                warrantyPrint = ticket.getLine(lines).isProductWarranty();
                return (true);
            }
            lines++;
        }
        return false;
    }

    public String getPickupString(TicketInfo pTicket) {
        if (pTicket == null) {
            return ("0");
        }
        String tmpPickupId = Integer.toString(pTicket.getPickupId());
        String pickupSize = (AppConfig.getInstance().getProperty("till.pickupsize"));
        if (pickupSize != null && (Integer.parseInt(pickupSize) >= tmpPickupId.length())) {
            while (tmpPickupId.length() < (Integer.parseInt(pickupSize))) {
                tmpPickupId = "0" + tmpPickupId;
            }
        }
        return (tmpPickupId);
    }

    private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext) {

        String sresource = dlSystem.getResourceAsXML(sresourcename);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(JPanelTicket.this);
        } else {

// if this is ticket does not have a pickup code assign on now            
            if (ticket.getPickupId() == 0) {
                try {
                    ticket.setPickupId(dlSales.getNextPickupIndex());
                } catch (BasicException e) {
                    ticket.setPickupId(0);
                }
            }
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                if (Boolean.parseBoolean(AppConfig.getInstance().getProperty("receipt.newlayout"))) {
                    script.put("taxes", ticket.getTaxLines());
                } else {
                    script.put("taxes", taxcollection);
                }
                script.put("taxeslogic", taxeslogic);
                script.put("ticket", ticket);
                script.put("place", ticketext);
                script.put("warranty", warrantyPrint);
                script.put("pickupid", getPickupString(ticket));

                refreshTicket();

                m_TTP.printTicket(script.eval(sresource).toString(), ticket);

            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            }
        }
    }

    private void printReport(String resourcefile, TicketInfo ticket, Object ticketext) {
        try {
            JasperReport jr;
            InputStream in = getClass().getResourceAsStream(resourcefile + ".ser");
            if (in == null) {
                // read and compile the report
                JasperDesign jd = JRXmlLoader.load(getClass().getResourceAsStream(resourcefile + ".jrxml"));
                jr = JasperCompileManager.compileReport(jd);
            } else {
                try (ObjectInputStream oin = new ObjectInputStream(in)) {
                    jr = (JasperReport) oin.readObject();
                }
            }
            // Construyo el mapa de los parametros.
            Map reportparams = new HashMap();
            // reportparams.put("ARG", params);
            try {
                reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(resourcefile + ".properties"));
            } catch (MissingResourceException e) {
            }
            reportparams.put("TAXESLOGIC", taxeslogic);

            Map reportfields = new HashMap();
            reportfields.put("TICKET", ticket);
            reportfields.put("PLACE", ticketext);

            JasperPrint jp = JasperFillManager.fillReport(jr, reportparams, new JRMapArrayDataSource(new Object[]{reportfields}));

            PrintService service = ReportUtils.getPrintService(AppConfig.getInstance().getProperty("machine.printername"));

            JRPrinterAWT300.printPages(jp, 0, jp.getPages().size() - 1, service);

        } catch (JRException | IOException | ClassNotFoundException e) {
            // MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadreport"), e);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, resourcefile + ": " + AppLocal.getIntString("message.cannotloadreport"), e);
            msg.show(this);
        }
    }

    private void visorTicketLine(TicketLineInfo oLine) {
        if (oLine == null) {
            m_App.getDeviceTicket().getDeviceDisplay().clearVisor();
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticketline", oLine);
                m_TTP.printTicket(script.eval(dlSystem.getResourceAsXML("Printer.TicketLine")).toString());
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
                msg.show(JPanelTicket.this);
            }
        }
    }

    public void kitchenOrderScreen() {
        kitchenOrderScreen(kitchenOrderId(), 1, true);
    }

    public void kitchenOrderScreen(String id) {
        kitchenOrderScreen(id, 1, true);
    }

    public void kitchenOrderScreen(Integer display, String ticketid) {
        kitchenOrderScreen(kitchenOrderId(), display, false);
    }

    public void kitchenOrderScreen(Integer display) {
        kitchenOrderScreen(kitchenOrderId(), display, false);
    }

    public String kitchenOrderId() {
        String id = "";
        if ((m_oTicket.getCustomer() != null)) {
            return m_oTicket.getCustomer().getName();
        } else if (m_oTicketExt != null) {
            return m_oTicketExt.toString();
        } else {
            if (m_oTicket.getPickupId() == 0) {
                try {
                    m_oTicket.setPickupId(dlSales.getNextPickupIndex());
                } catch (BasicException e) {
                    m_oTicket.setPickupId(0);
                }
            }
            return getPickupString(m_oTicket);
        }
    }

    public void kitchenOrderScreen(String id, Integer display, boolean primary) {
        // Create a UUID for this order for the kitchenorder table
        String orderUUID = UUID.randomUUID().toString();
        for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
            if ("No".equals(m_oTicket.getLine(i).getProperty("sendstatus"))) {
                if (primary) {
                    if ((m_oTicket.getLine(i).getProperty("display") == null) || (m_oTicket.getLine(i).getProperty("display") == "")) {
                        display = 1;
                    } else {
                        display = Integer.parseInt(m_oTicket.getLine(i).getProperty("display"));
                    }
                }
                try {
                    if (m_oTicket.getLine(i).isProductCom()) {
                        dlSystem.addOrder(UUID.randomUUID().toString(), orderUUID, (int) m_oTicket.getLine(i).getMultiply(), "+ " + m_oTicket.getLine(i).getProductName(),
                                m_oTicket.getLine(i).getProductAttSetInstDesc(), m_oTicket.getLine(i).getProperty("notes"), id, display, 1);
                    } else {
                        dlSystem.addOrder(UUID.randomUUID().toString(), orderUUID, (int) m_oTicket.getLine(i).getMultiply(), m_oTicket.getLine(i).getProductName(),
                                m_oTicket.getLine(i).getProductAttSetInstDesc(), m_oTicket.getLine(i).getProperty("notes"), id, display, 0);
                    }
                } catch (BasicException ex) {
                    Logger.getLogger(JPanelTicket.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {
        // resource here is guaranteed to be not null
        try {
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            return scr.evalScript(dlSystem.getResourceAsXML(resource), args);
        } catch (ScriptException e) {
            //  MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, resource + ": " + AppLocal.getIntString("message.cannotexecute"), e);
            msg.show(this);
            return msg;
        }
    }

    public void evalScriptAndRefresh(String resource, ScriptArg... args) {
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            evalScript(scr, resource, args);
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
        }
    }

    public void printTicket(String resource) {
        printTicket(resource, m_oTicket, m_oTicketExt);
    }

    private Object executeEventAndRefresh(String eventkey, ScriptArg... args) {
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            Object result = evalScript(scr, resource, args);
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
            return result;
        }
    }

    private Object executeEvent(TicketInfo ticket, Object ticketext, String eventkey, ScriptArg... args) {
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(ticket, ticketext);
            return evalScript(scr, resource, args);
        }
    }

    public String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    public BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }

    private void setSelectedIndex(int i) {
        if (i >= 0 && i < m_oTicket.getLinesCount()) {
            m_ticketlines.setSelectedIndex(i);
        } else if (m_oTicket.getLinesCount() > 0) {
            m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
        }
    }

    public static class ScriptArg {

        private final String key;
        private final Object value;

        public ScriptArg(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    private String setTempjPrice(String jPrice) {
        jPrice = jPrice.replace(".", "");
// remove all leading zeros from the string        
        long tempL = Long.parseLong(jPrice);
        jPrice = Long.toString(tempL);

        while (jPrice.length() < 3) {
            jPrice = "0" + jPrice;
        }
        return (jPrice.length() <= 2) ? jPrice : (new StringBuffer(jPrice).insert(jPrice.length() - 2, ".").toString());

    }

    public class ScriptObject {

        private final TicketInfo ticket;
        private final Object ticketext;
        private int selectedindex;

        private ScriptObject(TicketInfo ticket, Object ticketext) {
            this.ticket = ticket;
            this.ticketext = ticketext;
        }

        public double getInputValue() {
            if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                return JPanelTicket.this.getInputValue();
            } else {
                return 0.0;
            }
        }

        public int getSelectedIndex() {
            return selectedindex;
        }

        public void setSelectedIndex(int i) {
            selectedindex = i;
        }

        public void printReport(String resourcefile) {
            JPanelTicket.this.printReport(resourcefile, ticket, ticketext);
        }

        public void printTicket(String sresourcename) {
            JPanelTicket.this.printTicket(sresourcename, ticket, ticketext);
        }

        public Object evalScript(String code, ScriptArg... args) throws ScriptException {

            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            String sDBUser = AppConfig.getInstance().getProperty("db.user");
            String sDBPassword = AppConfig.getInstance().getProperty("db.password");

            if (sDBUser != null && sDBPassword != null && sDBPassword.startsWith("crypt:")) {
                AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser);
                sDBPassword = cypher.decrypt(sDBPassword.substring(6));
            }
            script.put("hostname", AppConfig.getInstance().getProperty("machine.hostname"));
            script.put("dbURL", AppConfig.getInstance().getProperty("db.URL"));
            script.put("dbUser", sDBUser);
            script.put("dbPassword", sDBPassword);
// End mod            
            script.put("ticket", ticket);
            script.put("place", ticketext);
            script.put("taxes", taxcollection);
            script.put("taxeslogic", taxeslogic);
            script.put("user", m_App.getAppUserView().getUser());
            script.put("sales", this);
            script.put("taxesinc", m_jaddtax.isSelected());
            script.put("warranty", warrantyPrint);
            script.put("pickupid", getPickupString(ticket));

            // more arguments
            for (ScriptArg arg : args) {
                script.put(arg.getKey(), arg.getValue());
            }
            return script.eval(code);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        m_jPanContainer = new javax.swing.JPanel();
        m_jOptions = new javax.swing.JPanel();
        m_jButtons = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        btnCustomer = new javax.swing.JButton();
        btnSplit = new javax.swing.JButton();
        jbtnLogout = new javax.swing.JButton();
        btnReprint1 = new javax.swing.JButton();
        m_jPanelScripts = new javax.swing.JPanel();
        m_jButtonsExt = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jbtnScale = new javax.swing.JButton();
        jbtnMooring = new javax.swing.JButton();
        j_btnKitchenPrt = new javax.swing.JButton();
        m_jPanelBag = new javax.swing.JPanel();
        m_jPanTicket = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jUp = new javax.swing.JButton();
        m_jDown = new javax.swing.JButton();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        jEditAttributes = new javax.swing.JButton();
        m_jPanelCentral = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jTicketId = new javax.swing.JLabel();
        m_jPanTotals = new javax.swing.JPanel();
        m_jLblTotalEuros3 = new javax.swing.JLabel();
        m_jLblTotalEuros2 = new javax.swing.JLabel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jSubtotalEuros = new javax.swing.JLabel();
        m_jTaxesEuros = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jContEntries = new javax.swing.JPanel();
        m_jPanEntries = new javax.swing.JPanel();
        m_jNumberKey = new uk.chromis.beans.JNumberKeys();
        jPanel9 = new javax.swing.JPanel();
        m_jPrice = new javax.swing.JLabel();
        m_jPor = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        m_jTax = new javax.swing.JComboBox();
        m_jaddtax = new javax.swing.JToggleButton();
        m_jKeyFactory = new javax.swing.JTextField();
        catcontainer = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 204, 153));
        setLayout(new java.awt.CardLayout());

        m_jPanContainer.setLayout(new java.awt.BorderLayout());

        m_jOptions.setLayout(new java.awt.BorderLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/customer_add_sml.png"))); // NOI18N
        jButton1.setToolTipText("Go to Customers");
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setMargin(new java.awt.Insets(0, 4, 0, 4));
        jButton1.setMaximumSize(new java.awt.Dimension(50, 40));
        jButton1.setMinimumSize(new java.awt.Dimension(50, 40));
        jButton1.setPreferredSize(new java.awt.Dimension(50, 40));
        jButton1.setRequestFocusEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/customer_sml.png"))); // NOI18N
        btnCustomer.setToolTipText("Find Customers");
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnCustomer.setMaximumSize(new java.awt.Dimension(50, 40));
        btnCustomer.setMinimumSize(new java.awt.Dimension(50, 40));
        btnCustomer.setPreferredSize(new java.awt.Dimension(50, 40));
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });

        btnSplit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/sale_split_sml.png"))); // NOI18N
        btnSplit.setToolTipText("Split Sale");
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnSplit.setMaximumSize(new java.awt.Dimension(50, 40));
        btnSplit.setMinimumSize(new java.awt.Dimension(50, 40));
        btnSplit.setPreferredSize(new java.awt.Dimension(50, 40));
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });

        jbtnLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/logout.png"))); // NOI18N
        jbtnLogout.setToolTipText("Logout");
        jbtnLogout.setFocusPainted(false);
        jbtnLogout.setFocusable(false);
        jbtnLogout.setMargin(new java.awt.Insets(0, 4, 0, 4));
        jbtnLogout.setMaximumSize(new java.awt.Dimension(50, 40));
        jbtnLogout.setMinimumSize(new java.awt.Dimension(50, 40));
        jbtnLogout.setPreferredSize(new java.awt.Dimension(50, 40));
        jbtnLogout.setRequestFocusEnabled(false);
        jbtnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogout(evt);
            }
        });

        btnReprint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/reprint.png"))); // NOI18N
        btnReprint1.setToolTipText("Reprint Last Ticket");
        btnReprint1.setFocusPainted(false);
        btnReprint1.setFocusable(false);
        btnReprint1.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnReprint1.setMaximumSize(new java.awt.Dimension(50, 40));
        btnReprint1.setMinimumSize(new java.awt.Dimension(50, 40));
        btnReprint1.setPreferredSize(new java.awt.Dimension(50, 40));
        btnReprint1.setRequestFocusEnabled(false);
        btnReprint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReprintActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout m_jButtonsLayout = new javax.swing.GroupLayout(m_jButtons);
        m_jButtons.setLayout(m_jButtonsLayout);
        m_jButtonsLayout.setHorizontalGroup(
            m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jButtonsLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jbtnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReprint1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );
        m_jButtonsLayout.setVerticalGroup(
            m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jButtonsLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnReprint1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        m_jOptions.add(m_jButtons, java.awt.BorderLayout.LINE_START);

        m_jPanelScripts.setLayout(new java.awt.BorderLayout());

        m_jButtonsExt.setLayout(new javax.swing.BoxLayout(m_jButtonsExt, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setMinimumSize(new java.awt.Dimension(235, 50));

        m_jbtnScale.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jbtnScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/scale.png"))); // NOI18N
        m_jbtnScale.setText(AppLocal.getIntString("button.scale")); // NOI18N
        m_jbtnScale.setToolTipText("Scale");
        m_jbtnScale.setFocusPainted(false);
        m_jbtnScale.setFocusable(false);
        m_jbtnScale.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnScale.setMaximumSize(new java.awt.Dimension(85, 44));
        m_jbtnScale.setMinimumSize(new java.awt.Dimension(85, 44));
        m_jbtnScale.setPreferredSize(new java.awt.Dimension(85, 40));
        m_jbtnScale.setRequestFocusEnabled(false);
        m_jbtnScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnScaleActionPerformed(evt);
            }
        });
        jPanel1.add(m_jbtnScale);

        jbtnMooring.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jbtnMooring.setText(bundle.getString("button.moorings")); // NOI18N
        jbtnMooring.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jbtnMooring.setMaximumSize(new java.awt.Dimension(80, 40));
        jbtnMooring.setMinimumSize(new java.awt.Dimension(80, 40));
        jbtnMooring.setPreferredSize(new java.awt.Dimension(80, 40));
        jbtnMooring.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnMooringActionPerformed(evt);
            }
        });
        jPanel1.add(jbtnMooring);

        j_btnKitchenPrt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/printer24.png"))); // NOI18N
        j_btnKitchenPrt.setText(bundle.getString("button.sendorder")); // NOI18N
        j_btnKitchenPrt.setToolTipText("Send to Kichen Printer");
        j_btnKitchenPrt.setMargin(new java.awt.Insets(0, 4, 0, 4));
        j_btnKitchenPrt.setMaximumSize(new java.awt.Dimension(50, 40));
        j_btnKitchenPrt.setMinimumSize(new java.awt.Dimension(50, 40));
        j_btnKitchenPrt.setPreferredSize(new java.awt.Dimension(50, 40));
        j_btnKitchenPrt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_btnKitchenPrtActionPerformed(evt);
            }
        });
        jPanel1.add(j_btnKitchenPrt);

        m_jButtonsExt.add(jPanel1);

        m_jPanelScripts.add(m_jButtonsExt, java.awt.BorderLayout.LINE_END);

        m_jOptions.add(m_jPanelScripts, java.awt.BorderLayout.LINE_END);

        m_jPanelBag.setPreferredSize(new java.awt.Dimension(0, 50));
        m_jPanelBag.setLayout(new java.awt.BorderLayout());
        m_jOptions.add(m_jPanelBag, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jOptions, java.awt.BorderLayout.NORTH);

        m_jPanTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanTicket.setLayout(new java.awt.BorderLayout());

        jPanel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(60, 200));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/1uparrow.png"))); // NOI18N
        m_jUp.setToolTipText("Scroll Up a Line");
        m_jUp.setFocusPainted(false);
        m_jUp.setFocusable(false);
        m_jUp.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jUp.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jUp.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jUp.setPreferredSize(new java.awt.Dimension(50, 36));
        m_jUp.setRequestFocusEnabled(false);
        m_jUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jUpActionPerformed(evt);
            }
        });
        jPanel2.add(m_jUp);

        m_jDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/1downarrow.png"))); // NOI18N
        m_jDown.setToolTipText("Scroll Down a Line");
        m_jDown.setFocusPainted(false);
        m_jDown.setFocusable(false);
        m_jDown.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDown.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDown.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDown.setPreferredSize(new java.awt.Dimension(50, 36));
        m_jDown.setRequestFocusEnabled(false);
        m_jDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDownActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDown);

        m_jDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/editdelete.png"))); // NOI18N
        m_jDelete.setToolTipText("Remove Line");
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDelete.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setPreferredSize(new java.awt.Dimension(50, 36));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDelete);

        m_jList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/search32.png"))); // NOI18N
        m_jList.setToolTipText("Product Search");
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jList.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jList.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jList.setPreferredSize(new java.awt.Dimension(50, 36));
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListActionPerformed(evt);
            }
        });
        jPanel2.add(m_jList);

        m_jEditLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/sale_editline.png"))); // NOI18N
        m_jEditLine.setToolTipText("Edit Line");
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditLine.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setPreferredSize(new java.awt.Dimension(50, 36));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditLine);

        jEditAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/attributes.png"))); // NOI18N
        jEditAttributes.setToolTipText("Choose Attributes");
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jEditAttributes.setMaximumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setMinimumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setPreferredSize(new java.awt.Dimension(50, 36));
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed(evt);
            }
        });
        jPanel2.add(jEditAttributes);

        jPanel5.add(jPanel2, java.awt.BorderLayout.NORTH);

        m_jPanTicket.add(jPanel5, java.awt.BorderLayout.LINE_END);

        m_jPanelCentral.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelCentral.setPreferredSize(new java.awt.Dimension(450, 240));
        m_jPanelCentral.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        m_jTicketId.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTicketId.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        m_jTicketId.setAutoscrolls(true);
        m_jTicketId.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(300, 40));
        m_jTicketId.setRequestFocusEnabled(false);
        m_jTicketId.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel4.add(m_jTicketId, java.awt.BorderLayout.CENTER);

        m_jPanTotals.setPreferredSize(new java.awt.Dimension(375, 60));
        m_jPanTotals.setLayout(new java.awt.GridLayout(2, 3, 4, 0));

        m_jLblTotalEuros3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros3.setLabelFor(m_jSubtotalEuros);
        m_jLblTotalEuros3.setText(AppLocal.getIntString("label.subtotalcash")); // NOI18N
        m_jPanTotals.add(m_jLblTotalEuros3);

        m_jLblTotalEuros2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros2.setLabelFor(m_jSubtotalEuros);
        m_jLblTotalEuros2.setText(AppLocal.getIntString("label.taxcash")); // NOI18N
        m_jPanTotals.add(m_jLblTotalEuros2);

        m_jLblTotalEuros1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros1.setLabelFor(m_jTotalEuros);
        m_jLblTotalEuros1.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        m_jPanTotals.add(m_jLblTotalEuros1);

        m_jSubtotalEuros.setBackground(m_jEditLine.getBackground());
        m_jSubtotalEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jSubtotalEuros.setForeground(m_jEditLine.getForeground());
        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jSubtotalEuros.setLabelFor(m_jSubtotalEuros);
        m_jSubtotalEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jSubtotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jSubtotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setOpaque(true);
        m_jSubtotalEuros.setPreferredSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jSubtotalEuros);

        m_jTaxesEuros.setBackground(m_jEditLine.getBackground());
        m_jTaxesEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jTaxesEuros.setForeground(m_jEditLine.getForeground());
        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTaxesEuros.setLabelFor(m_jTaxesEuros);
        m_jTaxesEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jTaxesEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTaxesEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setOpaque(true);
        m_jTaxesEuros.setPreferredSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jTaxesEuros);

        m_jTotalEuros.setBackground(m_jEditLine.getBackground());
        m_jTotalEuros.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jTotalEuros.setForeground(m_jEditLine.getForeground());
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTotalEuros.setLabelFor(m_jTotalEuros);
        m_jTotalEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jTotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(100, 25));
        m_jTotalEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jTotalEuros);

        jPanel4.add(m_jPanTotals, java.awt.BorderLayout.LINE_END);

        m_jPanelCentral.add(jPanel4, java.awt.BorderLayout.SOUTH);

        m_jPanTicket.add(m_jPanelCentral, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jPanTicket, java.awt.BorderLayout.CENTER);

        m_jContEntries.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jContEntries.setLayout(new java.awt.BorderLayout());

        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        m_jNumberKey.setMinimumSize(new java.awt.Dimension(200, 200));
        m_jNumberKey.setPreferredSize(new java.awt.Dimension(250, 250));
        m_jNumberKey.addJNumberEventListener(new uk.chromis.beans.JNumberEventListener() {
            public void keyPerformed(uk.chromis.beans.JNumberEvent evt) {
                m_jNumberKeyKeyPerformed(evt);
            }
        });
        m_jPanEntries.add(m_jNumberKey);

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        m_jPrice.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPrice.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 25));
        m_jPrice.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel9.add(m_jPrice, gridBagConstraints);

        m_jPor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPor.setOpaque(true);
        m_jPor.setPreferredSize(new java.awt.Dimension(22, 25));
        m_jPor.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jPor, gridBagConstraints);

        m_jEnter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/images/barcode.png"))); // NOI18N
        m_jEnter.setToolTipText("Get Barcode");
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jEnter, gridBagConstraints);

        m_jTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTax.setFocusable(false);
        m_jTax.setPreferredSize(new java.awt.Dimension(28, 25));
        m_jTax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel9.add(m_jTax, gridBagConstraints);

        m_jaddtax.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        m_jaddtax.setText("+");
        m_jaddtax.setFocusPainted(false);
        m_jaddtax.setFocusable(false);
        m_jaddtax.setPreferredSize(new java.awt.Dimension(40, 25));
        m_jaddtax.setRequestFocusEnabled(false);
        m_jaddtax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jaddtaxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel9.add(m_jaddtax, gridBagConstraints);

        m_jPanEntries.add(jPanel9);

        m_jKeyFactory.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setPreferredSize(new java.awt.Dimension(1, 1));
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
        });
        m_jPanEntries.add(m_jKeyFactory);

        m_jContEntries.add(m_jPanEntries, java.awt.BorderLayout.NORTH);

        m_jPanContainer.add(m_jContEntries, java.awt.BorderLayout.LINE_END);

        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        catcontainer.setLayout(new java.awt.BorderLayout());
        m_jPanContainer.add(catcontainer, java.awt.BorderLayout.SOUTH);

        add(m_jPanContainer, "ticket");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnScaleActionPerformed
        stateTransition('\u00a7');
    }//GEN-LAST:event_m_jbtnScaleActionPerformed

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditLineActionPerformed
        AutoLogoff.getInstance().deactivateTimer();
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo newline = JProductLineEdit.showMessage(this, m_App, m_oTicket.getLine(i));
                if (newline != null) {
                    // line has been modified
                    paintTicketLine(i, newline);
                    if (newline.getUpdated()) {
                        reLoadCatalog();
                    }
                }
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_m_jEditLineActionPerformed

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnterActionPerformed
        stateTransition('\n');
    }//GEN-LAST:event_m_jEnterActionPerformed

    private void m_jNumberKeyKeyPerformed(uk.chromis.beans.JNumberEvent evt) {//GEN-FIRST:event_m_jNumberKeyKeyPerformed
        stateTransition(evt.getKey());
    }//GEN-LAST:event_m_jNumberKeyKeyPerformed

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jKeyFactoryKeyTyped
        m_jKeyFactory.setText(null);
        stateTransition(evt.getKeyChar());
    }//GEN-LAST:event_m_jKeyFactoryKeyTyped

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDeleteActionPerformed
        int i = m_ticketlines.getSelectedIndex();
        if ((m_oTicket.getTicketType().equals(TicketType.REFUND))  && (!m_oTicket.getLine(i).isProductCom())) {
            JRefundLines.addBackLine(m_oTicket.getLine(i).printName(), m_oTicket.getLine(i).getMultiply(), m_oTicket.getLine(i).getPrice(), m_oTicket.getLine(i).getProperty("orgLine"));
            removeTicketLine(i);
            while (i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                JRefundLines.addBackLine(m_oTicket.getLine(i).printName(), m_oTicket.getLine(i).getMultiply(), m_oTicket.getLine(i).getPrice(), m_oTicket.getLine(i).getProperty("orgLine"));
                removeTicketLine(i);
            }
        } else if (m_oTicket.getTicketType().equals(TicketType.REFUND)) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    AppLocal.getIntString("message.deleteauxiliaryitem"),
                    "auxiliary Item", JOptionPane.WARNING_MESSAGE);
        } else if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            removeTicketLine(i); // elimino la linea           
        }
    }//GEN-LAST:event_m_jDeleteActionPerformed

    private void m_jUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jUpActionPerformed

        m_ticketlines.selectionUp();

    }//GEN-LAST:event_m_jUpActionPerformed

    private void m_jDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDownActionPerformed

        m_ticketlines.selectionDown();

    }//GEN-LAST:event_m_jDownActionPerformed

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListActionPerformed
        AutoLogoff.getInstance().deactivateTimer();
        ProductInfoExt prod = JProductFinder.showMessage(JPanelTicket.this, dlSales);
        if (prod != null) {
            buttonTransition(prod);
        }
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_m_jListActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        AutoLogoff.getInstance().deactivateTimer();
        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);
        finder.search(m_oTicket.getCustomer());
        finder.setVisible(true);

        try {
            if (finder.getSelectedCustomer() == null) {
                m_oTicket.setCustomer(null);
            } else {
                m_oTicket.setCustomer(dlSales.loadCustomerExt(finder.getSelectedCustomer().getId()));
                if ("restaurant".equals(AppConfig.getInstance().getProperty("machine.ticketsbag"))) {
                    restDB.setCustomerNameInTableByTicketId(dlSales.loadCustomerExt(finder.getSelectedCustomer().getId()).toString(), m_oTicket.getId());
                }
            }

        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
            msg.show(this);
        }
        AutoLogoff.getInstance().activateTimer();
        refreshTicket();
}//GEN-LAST:event_btnCustomerActionPerformed

    private void btnSplitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSplitActionPerformed
        AutoLogoff.getInstance().deactivateTimer();
        if (m_oTicket.getArticlesCount() > 1) {
            //read resource ticket.line and execute
            ReceiptSplit splitdialog = ReceiptSplit.getDialog(this, dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);

            TicketInfo ticket1 = m_oTicket.copyTicket();
            TicketInfo ticket2 = new TicketInfo();
            ticket2.setCustomer(m_oTicket.getCustomer());

            if (splitdialog.showDialog(ticket1, ticket2, m_oTicketExt)) {
                if (closeTicket(ticket2, m_oTicketExt)) {
                    setActiveTicket(ticket1, m_oTicketExt);
                }
            }
        }
        AutoLogoff.getInstance().activateTimer();
}//GEN-LAST:event_btnSplitActionPerformed

    private void jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEditAttributesActionPerformed
        // AutoLogoff.getInstance().deactivateTimer();
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo line = m_oTicket.getLine(i);
                JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    // The user pressed OK
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    paintTicketLine(i, line);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
                AutoLogoff.getInstance().activateTimer();
            }
        }
        //  AutoLogoff.getInstance().activateTimer();
}//GEN-LAST:event_jEditAttributesActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        AutoLogoff.getInstance().activateTimer();
// Show the custmer panel - this does deactivate
        {
            m_App.getAppUserView().showTask("uk.chromis.pos.customers.CustomersPanel");
        }
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jbtnMooringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnMooringActionPerformed
// Display vessel selection box on screen if reply is good add to the ticket
        AutoLogoff.getInstance().deactivateTimer();
        JMooringDetails mooring = JMooringDetails.getMooringDetails(this, m_App.getSession());
        mooring.setVisible(true);
        if (mooring.isCreate()) {
            if (((mooring.getVesselDays() > 0)) && ((mooring.getVesselSize() > 1))) {
                try {
                    ProductInfoExt vProduct = dlSales.getProductInfoByCode("BFeesDay1");
                    vProduct.setName("Berth Fees 1st Day " + mooring.getVesselName());
                    addTicketLine(vProduct, mooring.getVesselSize(), vProduct.getPriceSell());
                    if (mooring.getVesselDays() > 1) {
                        vProduct = dlSales.getProductInfoByCode("BFeesDay2");
                        vProduct.setName("Additional Days " + (mooring.getVesselDays() - 1));
                        addTicketLine(vProduct, mooring.getVesselSize() * (mooring.getVesselDays() - 1), vProduct.getPriceSell());
                    }
                    if (mooring.getVesselPower()) {
                        vProduct = dlSales.getProductInfoByCode("PowerSupplied");
                        addTicketLine(vProduct, mooring.getVesselDays(), vProduct.getPriceSell());
                    }
                } catch (BasicException e) {
                }
            }
        }
        refreshTicket();
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_jbtnMooringActionPerformed

    private void j_btnKitchenPrtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j_btnKitchenPrtActionPerformed
// John L - replace older SendOrder script
        AutoLogoff.getInstance().deactivateTimer();
        if (!m_oTicket.getTicketType().equals(TicketType.REFUND)) {
            String rScript = (dlSystem.getResourceAsText("script.SendOrder"));
            Interpreter i = new Interpreter();
            try {
                i.set("ticket", m_oTicket);
                i.set("place", m_oTicketExt);
                i.set("user", m_App.getAppUserView().getUser());
                i.set("sales", this);
                i.set("pickupid", m_oTicket.getPickupId());
                Object result;
                result = i.eval(rScript);

            } catch (EvalError ex) {
                Logger.getLogger(JPanelTicket.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

            AutoLogoff.getInstance().activateTimer();
// Autologoff after sending to kitchen if required
            // lets check what mode we are operating in               
            switch (AppConfig.getInstance().getProperty("machine.ticketsbag")) {
                case "restaurant":
                    if (autoLogoffEnabled && autoLogoffAfterKitchen) {
                        if (autoLogoffToTables) {
                            deactivate();
                            setActiveTicket(null, null);
                            break;
                        } else {
                            deactivate();
                            ((JRootApp) m_App).closeAppView();
                            break;
                        }
                    }
            }
        }
    }//GEN-LAST:event_j_btnKitchenPrtActionPerformed

    private void m_jaddtaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jaddtaxActionPerformed
        if ("+".equals(m_jaddtax.getText())) {
            m_jaddtax.setText("-");
        } else {
            m_jaddtax.setText("+");
        }
    }//GEN-LAST:event_m_jaddtaxActionPerformed

    private void btnLogout(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogout
        AutoLogoff.getInstance().deactivateTimer();
        deactivate();
        ((JRootApp) m_App).closeAppView();
    }//GEN-LAST:event_btnLogout

    private void btnReprintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReprintActionPerformed
        AutoLogoff.getInstance().deactivateTimer();
// test if there is valid ticket in the system at this till to be printed
        if (AppConfig.getInstance().getProperty("lastticket.number") != null) {
            try {
                TicketInfo ticket = dlSales.loadTicket(Integer.parseInt((AppConfig.getInstance().getProperty("lastticket.type"))), Integer.parseInt((AppConfig.getInstance().getProperty("lastticket.number"))));
                if (ticket == null) {
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame, AppLocal.getIntString("message.notexiststicket"), AppLocal.getIntString("message.notexiststickettitle"), JOptionPane.WARNING_MESSAGE);
                } else {
                    m_ticket = ticket;
                    m_ticketCopy = null;
                    try {
                        taxeslogic.calculateTaxes(m_ticket);
                        TicketTaxInfo[] taxlist = m_ticket.getTaxLines();
                    } catch (TaxesException ex) {
                    }
                    printTicket("Printer.ReprintLastTicket", m_ticket, null);
                }
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"), e);
                msg.show(this);
            }
        }
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_btnReprintActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnReprint1;
    private javax.swing.JButton btnSplit;
    private javax.swing.JPanel catcontainer;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JButton j_btnKitchenPrt;
    private javax.swing.JButton jbtnLogout;
    private javax.swing.JButton jbtnMooring;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jButtonsExt;
    private javax.swing.JPanel m_jContEntries;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDown;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jLblTotalEuros2;
    private javax.swing.JLabel m_jLblTotalEuros3;
    private javax.swing.JButton m_jList;
    private uk.chromis.beans.JNumberKeys m_jNumberKey;
    private javax.swing.JPanel m_jOptions;
    private javax.swing.JPanel m_jPanContainer;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanTicket;
    private javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelCentral;
    private javax.swing.JPanel m_jPanelScripts;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTaxesEuros;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JButton m_jUp;
    private javax.swing.JToggleButton m_jaddtax;
    private javax.swing.JButton m_jbtnScale;
    // End of variables declaration//GEN-END:variables

}
