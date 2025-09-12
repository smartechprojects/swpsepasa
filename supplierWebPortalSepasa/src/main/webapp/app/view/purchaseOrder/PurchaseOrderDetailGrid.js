function sortByKey(array, key) {
    return array.sort(function(a, b) {
        var x = a[key]; var y = b[key];
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
};

function renderTip(val, meta, rec, rowIndex, colIndex, store) {
	var comments = "";
	var firstLineNotes ="";
	if(rec.data.purchaseOrderNotes){
		sortByKey(rec.data.purchaseOrderNotes, "instruction");
		for(var i = 0; i < rec.data.purchaseOrderNotes.length; i++){
			if(i == 0){
				firstLineNotes = rec.data.purchaseOrderNotes[i].lineText + "...";
			}
			comments = comments + " " + rec.data.purchaseOrderNotes[i].lineText;
			comments = comments.replace(/"/g, '');
		}

	}
	
	if(comments != ""){
	    meta.tdAttr = 'data-qtip="' + comments + '"';
	    return firstLineNotes;
	}else{
		return "";
	}

};
Ext.QuickTips.init();

var store = Ext.create('Ext.data.Store', {
	model: 'PurchaseOrderDetail',
	autoLoad: false,
	pageSize: 20,
	remoteSort: false,
	proxy: {
		type: 'memory',
		reader: {
			type: 'json',
			root: 'root'
		}, writer: {
			type: 'json',
			writeAllFields: true,
			encode: false
		}
	}, root: {
		expanded: false
	},
	sortOnLoad: true,
    sorters: [{
        property: 'lineNumber',
        direction: 'ASC'
    }]
    
});


Ext.define('SupplierApp.view.purchaseOrder.PurchaseOrderDetailGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.purchaseOrderDetailGrid',
    loadMask: true,
	frame:false,
	border:false,
	cls: 'extra-large-cell-grid',  
	store:store,
	scroll : true,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
        this.columns = [{
						    width: 100,
						    dataIndex: 'status',
						    hidden:true
						},{
						    text     : SuppAppMsg.purchaseTitle26,
						    width: 50,
						    dataIndex: 'lineNumber'
						},{
						    text     : 'Código',
						    width: 70,
						    dataIndex: 'itemNumber',
						    hidden:true
						},{
						    text     : SuppAppMsg.paymentTitle1,
						    width: 70,
						    dataIndex: 'orderCompany'
						},{
						    text     : SuppAppMsg.purchaseTitle27,
						    width: 140,
						    dataIndex: 'glOffSet'
						},{
						    text     : 'Cantidad',
						    width: 70,
						    dataIndex: 'quantity',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
						    text     : 'UOM',
						    width: 50,
						    dataIndex: 'uom',
						    hidden:true
						},{
						    text     : SuppAppMsg.purchaseTitle29,
						    width: 300,
						    dataIndex: 'itemDescription'
						},{
						    text     : 'Código SAT',
						    width: 80,
						    dataIndex: 'codigoSat',
						    hidden:true
						},{
						    text     : 'Precio unitario',
						    width: 100,
						    dataIndex: 'unitCost',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
						    text     : SuppAppMsg.purchaseOrderÌmporteTotal,
						    width: 80,
						    dataIndex: 'extendedPrice',
						    renderer : Ext.util.Format.numberRenderer('0,0.00')
						},{
							text     : 'Recibido',
						    width: 90,
						    dataIndex: 'received',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
							text     : 'Rechazado',
						    width: 90,
						    dataIndex: 'rejected',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
						    text     : 'Por recibir',
						    width: 100,
						    dataIndex: 'toReceive',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    //hidden:role=='ROLE_SUPPLIER' || role== 'ROLE_SUPPLIER_OPEN'?true:false,
						    hidden:true
						},{
						    text     : 'Por rechazar',
						    width: 100,
						    dataIndex: 'toReject',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    //hidden:role=='ROLE_SUPPLIER' || role== 'ROLE_SUPPLIER_OPEN'?true:false,
							hidden:true
						},{
						    text     : 'Pendiente',
						    width: 80,
						    hidden:false,
						    dataIndex: 'pending',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
						    text     : 'Importe recibido',
						    width: 100,
						    dataIndex: 'amuntReceived',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
						    text     : 'Importe pendiente',
						    width: 100,
						    dataIndex: 'openAmount',
						    renderer : Ext.util.Format.numberRenderer('0,0.00'),
						    hidden:true
						},{
						    width: 160,
						    text: 'Motivo de rechazo',
						    dataIndex: 'reason',
						    hidden:true
						},{
						    text     : 'Notas',
						    width: 180,
						    dataIndex: 'notes',
						    hidden:true
						},{
						    text     : SuppAppMsg.purchaseTitle30,
						    width: 550,
						    dataIndex: 'purchaseOrderNotes',
						    renderer: renderTip
						},{
						    dataIndex: 'taxCode',
						    hidden:true
						},{
						    dataIndex: 'taxable',
						    hidden:true
						}];
        
        this.tbar = [ {
			iconCls : 'icon-save',
			itemId : 'updateOrder',
			id : 'updateOrder',
			text : SuppAppMsg.purchaseTitle31,
			action : 'updateOrder',
			hidden:true
		}];
              
        this.callParent(arguments);
    }
	
});