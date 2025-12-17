Ext.define('SupplierApp.view.receipt.ReceiptMultiOrderForm' ,{
	extend: 'Ext.form.Panel',
	alias : 'widget.receiptMultiOrderForm',
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
     				labelWidth : 80,
					name: 'orderCompany',
					width:260
				},{
					fieldLabel: SuppAppMsg.suppliersNumber,
					labelWidth : 100,
					name: 'addressNumber',
					width:260
				},{
					fieldLabel: SuppAppMsg.purchaseTitle5,
					labelWidth : 80,
					name: 'orderType',
					width:200
				},{
					fieldLabel: SuppAppMsg.purchaseOrderNumber,
					labelWidth : 80,
					name: 'orderNumber',
					width:800
				},{
					fieldLabel: SuppAppMsg.purchaseOrderÌmporteTotal,
					name: 'orderAmount',
					xtype: 'numericfield',
					currencySymbol: '$ ',
                    useThousandSeparator: true,
                    decimalSeparator: '.',
                    thousandSeparator: ',',
                    alwaysDisplayDecimals: true,
                    width:320,
					hidden: true
				},{
     				xtype : 'hidden',
     				name : 'optionMultiOrderIds',
     				id:'optionMultiOrderIds',
				},{
     				xtype : 'hidden',
     				name : 'optionMultiOrderType',
     				id:'optionMultiOrderType',
				}]
			}];
			
			this.tbar=[      
				 {
			    	  iconCls: 'icon-save',
			    	  id: 'uploadReceiptMultiOrderInvoice',
			    	  itemId: 'uploadReceiptMultiOrderInvoice',
			    	  text: SuppAppMsg.purchaseTitle36,
			    	  action: 'uploadReceiptMultiOrderInvoice',
			    	  hidden:true
			      },{
			    	  iconCls: 'icon-save',
			    	  id: 'showOutSourcingMultiOrderWindow',
			    	  itemId: 'showOutSourcingMultiOrderWindow',
			    	  text: SuppAppMsg.outsourcingButton,
			    	  action: 'showOutSourcingMultiOrderWindow',
			    	  hidden:true
			      },{
			    	  iconCls: 'icon-save',
			    	  id: 'uploadReceiptMultiOrderCreditNote',
			    	  itemId: 'uploadReceiptMultiOrderCreditNote',
			    	  text: SuppAppMsg.purchaseTitle57,
			    	  action: 'uploadReceiptMultiOrderCreditNote',
			    	  hidden:true
			      },{
			    	  iconCls: 'icon-save',
			    	  id: 'uploadReceiptMultiOrderInvoiceZip',
			    	  itemId: 'uploadReceiptMultiOrderInvoiceZip',
			    	  text: SuppAppMsg.supplierLoad + ' Zip',
			    	  action: 'uploadReceiptMultiOrderInvoiceZip',
			    	  hidden: true
			      }];
		  this.callParent(arguments);	    
	  }

});