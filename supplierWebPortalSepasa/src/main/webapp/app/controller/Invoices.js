Ext.define('SupplierApp.controller.Invoices', {
    extend: 'Ext.app.Controller',
    stores: ['Invoices','ReportInvoices'],
    models: ['ReportInvoices'],
    views: ['invoices.InvoicesPanel','invoices.InvoicesGrid'],
    refs: [{
        	ref: 'invoicesGrid',
        	selector: 'invoicesGrid'
	    },{
        	ref: 'invoiceReportPanel',
        	selector: 'invoiceReportPanel'
	    },{
        	ref: 'invoiceReportGrid',
        	selector: 'invoiceReportGrid'
	    }],
 
    init: function() {
   	
    	//this.winLoadInv2=null;
    	
        this.control({
			'invoicesGrid button[action=invoiceSearch]' : {
				click : this.invoiceSearch
			},
		    'invoicesGrid button[action=downloadInv]' : {
				click : this.downloadInv
			},
			'invoiceReportGrid button[action=reportInvoiceSearch]' : {
				click : this.reportInvoiceSearch
			},
			'invoiceReportGrid button[action=downloadReportInvoices]' : {
				click : this.downloadReportInvoices
			},
        });
    },
    
    reportInvoiceSearch: function(button) {
    	
    	var grid = this.getInvoiceReportGrid();
    	var store = grid.getStore();

    	var supNumber = Ext.getCmp('supNumberInvoiceReport').getValue() == ''?'':Ext.getCmp('supNumberInvoiceReport').getValue();   	
    	var rfcEmisorReport = Ext.getCmp('rfcEmisorReport').getValue() == ''?'':Ext.getCmp('rfcEmisorReport').getValue();
    	var invoiceReportFromDate = Ext.getCmp('invoiceReportFromDate').getValue();
    	var invoiceReportToDate = Ext.getCmp('invoiceReportToDate').getValue();
    	var rfcReceptorReport = Ext.getCmp('rfcReceptorReport').getValue() == ''?'':Ext.getCmp('rfcReceptorReport').getValue();
    	var moduleReport = Ext.getCmp('comboModuleType').getValue();
    	store.removeAll();    	
    	store.proxy.extraParams = {
    							supplierNumber : supNumber?supNumber:"",
    							rfc: rfcEmisorReport?rfcEmisorReport:"",						
    							poFromDate : invoiceReportFromDate?invoiceReportFromDate:null,
    						    poToDate : invoiceReportToDate?invoiceReportToDate:null,
    						    rfcReceptor : rfcReceptorReport?rfcReceptorReport:"",
    						    module : moduleReport ? moduleReport : ""
    	    			        }
    	store.loadPage(1);
    	grid.getView().refresh()
    	
    },
    
    downloadReportInvoices: function(button) {
    	
    	var supNumber = Ext.getCmp('supNumberInvoiceReport').getValue() == ''?'':Ext.getCmp('supNumberInvoiceReport').getValue();   	
    	var rfcEmisorReport = Ext.getCmp('rfcEmisorReport').getValue() == ''?'':Ext.getCmp('rfcEmisorReport').getValue();
    	var invoiceReportFromDate = Ext.getCmp('invoiceReportFromDate').getValue();
    	var invoiceReportToDate = Ext.getCmp('invoiceReportToDate').getValue();
    	var rfcReceptorReport = Ext.getCmp('rfcReceptorReport').getValue() == ''?'':Ext.getCmp('rfcReceptorReport').getValue();
    	
    	if(invoiceReportFromDate == null || invoiceReportToDate == null){
    		Ext.Msg.alert('Error', "Seleccionar rango de fechas para descargar reporte<br>");
    		return false;
    	}
    	
    	var fromDate = invoiceReportFromDate.getFullYear() + "-" + (invoiceReportFromDate.getMonth() + 1) + "-" + invoiceReportFromDate.getDate() + "T00:00:00";
    	var toDate = invoiceReportToDate.getFullYear() + "-" + (invoiceReportToDate.getMonth() + 1) + "-" + invoiceReportToDate.getDate() + "T00:00:00";
    	
    	var form = Ext.create('Ext.form.Panel', {
            standardSubmit : true,
            method : 'POST',
            url : 'invoice/downloadReportInvoicesToExcel.action',
        });
    	
        form.updateLayout();
        form.submit({
            params : {	    	    	
            	supplierNumber : supNumber?supNumber:"",
				rfc: rfcEmisorReport?rfcEmisorReport:"",						
				poFromDate : fromDate?fromDate:null,
			    poToDate : toDate?toDate:null,
			    rfcReceptor : rfcReceptorReport?rfcReceptorReport:"",
			    module : module ? module : ""
          	  }
        });
        
        Ext.Msg.alert('', "Espere a que se descargue el reporte<br>");
    	/*Ext.Ajax.request({
			 url: 'invoice/downloadReportInvoicesToExcel.action',
			    method: 'POST',
			    params: {
			    	supplierNumber : supNumber?supNumber:"",
					rfc: rfcEmisorReport?rfcEmisorReport:"",						
					poFromDate : invoiceReportFromDate?invoiceReportFromDate:null,
				    poToDate : invoiceReportToDate?invoiceReportToDate:null 		    	
	        },
		    success: function(fp, o) {
		    	debugger;
		    	waitBox.hide();
		    	var res = fp.responseText;
		    	
			    var link = document.createElement('a');
			    link.innerHTML = 'Facturas';
			    link.download = 'Facturas.zip';
			    link.href = 'data:application/zip;base64,' + res;
               link.style.display = 'none';
			    document.body.appendChild(link);
			    
			    link.click();
		
		    },
		    failure: function(fp, o) {
		    	waitBox.hide();
		    	Ext.MessageBox.show({
	                title: 'Error',
	                msg: SuppAppMsg.invoiceDownloadError,
	                buttons: Ext.Msg.OK
	            });
		    }
		}); */

    },

    
    invoiceSearch: function(button) {
    	var moduleReport = Ext.getCmp('comboModuleType').getValue();
    	if(moduleReport == null){
    		Ext.MessageBox.alert({ maxWidth: 700, minWidth: 650, title: SuppAppMsg.supplierMsgValidationLoad, msg: SuppAppMsg.validateModuleMsg });
    		return false;
    	}
    	
    	var grid = this.getInvoicesGrid();
    	var store = grid.getStore();
    	var supNumber = Ext.getCmp('supNumberInvoice').getValue() == ''?'':Ext.getCmp('supNumberInvoice').getValue();   	
    	var invoiceUUID = Ext.getCmp('invoiceUUID').getValue() == ''?'':Ext.getCmp('invoiceUUID').getValue();
    	var pFolio = Ext.getCmp('invoiceFolio').getValue() == ''?'':Ext.getCmp('invoiceFolio').getValue();
    	var poFromDate = Ext.getCmp('invoiceFromDate').getValue();
    	var poToDate = Ext.getCmp('invoiceToDate').getValue();

    	store.removeAll();    	
    	store.proxy.extraParams = {
    							supplierNumber : supNumber?supNumber:"",
    							uuid: invoiceUUID?invoiceUUID:"",
    							pFolio:pFolio?pFolio:"",   								
    							poFromDate : poFromDate?poFromDate:null,
    						    poToDate : poToDate?poToDate:null ,
    						    module : moduleReport?moduleReport:""
    	    			        }
    	store.loadPage(1);
    	grid.getView().refresh()
    	
    },
    
    downloadInv : function(grid, rowIndex, colIndex, record) {
    	
    	var moduleReport = Ext.getCmp('comboModuleType').getValue();
    	if(moduleReport == null){
    		Ext.MessageBox.alert({ maxWidth: 700, minWidth: 650, title: SuppAppMsg.supplierMsgValidationLoad, msg: SuppAppMsg.validateModuleMsg });
    		return false;
    	}
    	var grid = this.getInvoicesGrid();
    	
    	var supNumber = Ext.getCmp('supNumberInvoice').getValue() == ''?'':Ext.getCmp('supNumberInvoice').getValue();   	
    	var invoiceUUID = Ext.getCmp('invoiceUUID').getValue() == ''?'':Ext.getCmp('invoiceUUID').getValue();
    	var pFolio = Ext.getCmp('invoiceFolio').getValue() == ''?'':Ext.getCmp('invoiceFolio').getValue();
    	var poFromDate = Ext.getCmp('invoiceFromDate').getValue();
    	var poToDate = Ext.getCmp('invoiceToDate').getValue();
    	var moduleReport = Ext.getCmp('comboModuleType').getValue();
    	
    	var waitBox = Ext.MessageBox.wait(SuppAppMsg.supplierProcessRequest);
		Ext.Ajax.request({
			 url: 'downloadAllsInvoicesZip.action',
			    method: 'POST',
			    params: {
			    	supplierNumber : supNumber?supNumber:"",
					uuid: invoiceUUID?invoiceUUID:"",
					pFolio:pFolio?pFolio:"",   								
					poFromDate : poFromDate?poFromDate:null,
					poToDate : poToDate?poToDate:null,
					module : moduleReport?moduleReport:""		
	        },
		    success: function(fp, o) {
		    	waitBox.hide();
		    	var res = fp.responseText;
		    	
			    var link = document.createElement('a');
			    link.innerHTML = 'Facturas';
			    link.download = 'Facturas.zip';
			    link.href = 'data:application/zip;base64,' + res;
                link.style.display = 'none';
			    document.body.appendChild(link);
			    
			    link.click();
		
		    },
		    failure: function(fp, o) {
		    	waitBox.hide();
		    	Ext.MessageBox.show({
	                title: 'Error',
	                msg: SuppAppMsg.invoiceDownloadError,
	                buttons: Ext.Msg.OK
	            });
		    }
		}); 

    }
    
});