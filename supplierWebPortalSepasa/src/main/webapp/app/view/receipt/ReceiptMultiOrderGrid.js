Ext.define('SupplierApp.view.receipt.ReceiptMultiOrderGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.receiptMultiOrderGrid',
    loadMask: true,
	frame:false,
	border:false,
	selModel: {
        checkOnly: true,
        mode: 'SIMPLE',
        showHeaderCheckbox: false,
        injectCheckbox:'first',
        renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
	            metaData.tdCls = Ext.baseCSSPrefix + 'grid-cell-special';
	            var html = '';
	            if (record.data.uuid != null && record.data.uuid != '') {
	                html = '';
	            } else {
	            	html = '<div class="' + Ext.baseCSSPrefix + 'grid-row-checker"> </div>';
	            }
	            return html;
        }
    },
    selType: 'checkboxmodel',
	cls: 'extra-large-cell-grid', 
    store:'Receipt',
	scroll : true,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
	
    initComponent: function() {
 
        this.columns = [
    	{
            hidden:true,
            dataIndex: 'id',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
        },{
            text     : 'Sts',
            dataIndex: 'paymentStatus',
            width: 50
        },{
            text     : 'Sec',
            dataIndex: 'receiptLine',
            width: 60
		},{
            text     : SuppAppMsg.purchaseOrderNumber,
            width: 80,
            dataIndex: 'orderNumber'
        },{
            text     : SuppAppMsg.purchaseOrderType,
            width: 40,
            dataIndex: 'orderType'
        },{
            text     : SuppAppMsg.receiptTitle1,
            dataIndex: 'documentNumber',
            width: 90
        },{
            text     : SuppAppMsg.receiptTitle2,
            width: 90,
            dataIndex: 'receiptDate',
            renderer : Ext.util.Format.dateRenderer("d-m-Y")
        },{
            text     : SuppAppMsg.receiptTitle3,
            dataIndex: 'amountReceived',
            renderer : Ext.util.Format.numberRenderer('0,000.00')
        },{
            text     : SuppAppMsg.receiptTitle4,
            dataIndex: 'foreignAmountReceived',
            renderer : Ext.util.Format.numberRenderer('0,000.00')
        },{
            text     : SuppAppMsg.purchaseOrderCurrency,
            dataIndex: 'currencyCode',
            width: 60
        },{
            text     : SuppAppMsg.purchaseTitle43,
            dataIndex: 'quantityReceived',
            //renderer : Ext.util.Format.numberRenderer('0,000.00')
            
            renderer: function(value, metaData, record, row, col, store, gridView){
				if(value) {
					value = value / 10000;
					return Ext.util.Format.number(value, '0,000.00');
				} else {
					return null;
				}
			}
            
            
        },{
            text     : 'UOM',
            dataIndex: 'uom',
            width: 50
        },{
            text     : 'Remark',
            dataIndex: 'remark',
            width: 90
        },{
            text     : SuppAppMsg.receiptTitle8,
            dataIndex: 'receiptType',
            width: 40
        },{
            text     : SuppAppMsg.receiptTitle12,
            dataIndex: 'taxCode',
            width: 90
        },{ 
            text     : SuppAppMsg.fiscalTitle3,
            dataIndex: 'folio',
            width: 80
        },{
            text     : SuppAppMsg.fiscalTitle4,
            dataIndex: 'serie',
            width: 80
        },{        	
            text     : SuppAppMsg.receiptTitle10,
            dataIndex: 'invDate',
            width: 110,
			renderer: function(value, metaData, record, row, col, store, gridView){
				if(value) {
					return Ext.util.Format.date(new Date(value), 'd-m-Y');
				} else {
					return null;
				}
			}
        },{
            text     : SuppAppMsg.receiptTitle11,
            dataIndex: 'uploadInvDate',
            width: 110,
			renderer: function(value, metaData, record, row, col, store, gridView){
				if(value) {
					return Ext.util.Format.date(new Date(value), 'd-m-Y');
				} else {
					return null;
				}
			}
        },{
            text     : SuppAppMsg.receiptTitle5,
            dataIndex: 'estPmtDate',
            width: 110,
            hidden: true,
            hideable: false,//Para que no aparezca en la lista de "Columnas"
			renderer: function(value, metaData, record, row, col, store, gridView){
				if(value) {
					return Ext.util.Format.date(new Date(value), 'd-m-Y');
				} else {
					return null;
				}
			}
        },{
            text     : SuppAppMsg.receiptTitle5,
            dataIndex: 'estPmtDateStr',
            width: 110
        },{
            text     : SuppAppMsg.receiptTitle9,
            dataIndex: 'paymentDate',
            width: 80,
			renderer: function(value, metaData, record, row, col, store, gridView){
				if(value) {
					return Ext.util.Format.date(new Date(value), 'd-m-Y');
				} else {
					return null;
				}
			}
        },{
            text     : 'UUID',
            dataIndex: 'uuid',
            width: 290,
        }];
      
        this.callParent(arguments);
    }
});