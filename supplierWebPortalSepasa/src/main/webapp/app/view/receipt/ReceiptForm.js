Ext.define('SupplierApp.view.receipt.ReceiptForm' ,{
	extend: 'Ext.form.Panel',
	alias : 'widget.receiptForm',
	border:true,
	  initComponent: function() {		  
			this.items= [{
				xtype: 'container',
				layout: 'hbox',
				margin: '15 15 0 5',
        		style:'border-bottom: 1px dotted #fff;padding-bottom:10px',
				defaults: { 
					labelWidth : 100,
					align: 'stretch',
					fieldStyle: 'padding-bottom:5px;font-size:18px;vertical-align:top;border:none;background:transparent;color:black;font-weight:bold',
					xtype:'textfield',
					readOnly:true,
					width:220
				},
				items       :[{
					xtype: 'hidden',
					name: 'id'
				},{
     				xtype : 'hidden',
     				name : 'createdBy',
     			},{
     				xtype : 'hidden',
     				name : 'creationDate',
     				format : 'd-M-Y',
     			},{
     				fieldLabel: SuppAppMsg.purchaseOrderCompany,
					name: 'orderCompany'
				},{
					fieldLabel: SuppAppMsg.purchaseOrderNumber,
					name: 'orderNumber'
				},{
					fieldLabel: SuppAppMsg.purchaseTitle5,
					labelWidth : 90,
					name: 'orderType',
					width:170
				},{
					fieldLabel: SuppAppMsg.suppliersNumber,
					labelWidth : 130,
					name: 'addressNumber',
					width:280
				},{
					fieldLabel: SuppAppMsg.purchaseOrderÌmporteTotal,
					name: 'orderAmount',
					xtype: 'numericfield',
					currencySymbol: '$ ',
                    useThousandSeparator: true,
                    decimalSeparator: '.',
                    thousandSeparator: ',',
                    alwaysDisplayDecimals: true,
                    width:320
				},{
					name: 'supplierCountry',
					hidden:true
				},{
     				xtype : 'hidden',
     				name : 'optionType',
     				id:'optionType',
				}]
			}];
			
			this.tbar=[      
				 {
			    	  iconCls: 'icon-save',
			    	  id: 'uploadReceiptInvoice',
			    	  itemId: 'uploadReceiptInvoice',
			    	  text: SuppAppMsg.purchaseTitle36,
			    	  action: 'uploadReceiptInvoice',
			    	  hidden:true
			      },{
			    	  iconCls: 'icon-save',
			    	  id: 'showOutSourcingWindow',
			    	  itemId: 'showOutSourcingWindow',
			    	  text: SuppAppMsg.outsourcingButton,
			    	  action: 'showOutSourcingWindow',
			    	  hidden:true
			      },{
			    	  iconCls: 'icon-save',
			    	  id: 'uploadReceiptCreditNote',
			    	  itemId: 'uploadReceiptCreditNote',
			    	  text: SuppAppMsg.purchaseTitle57,
			    	  action: 'uploadReceiptCreditNote',
			    	  hidden:true
			      },{
			    	  iconCls: 'icon-save',
			    	  id: 'uploadReceiptInvoiceZip',
			    	  itemId: 'uploadReceiptInvoiceZip',
			    	  text: SuppAppMsg.supplierLoad + ' Zip',
			    	  action: 'uploadReceiptInvoiceZip',
			    	  hidden: true
			      }];
		  this.callParent(arguments);	    
	  }

});