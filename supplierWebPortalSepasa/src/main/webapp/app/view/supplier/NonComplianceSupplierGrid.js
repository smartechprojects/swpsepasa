Ext.define('SupplierApp.view.supplier.NonComplianceSupplierGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.nonComplianceSupplierGrid',
    loadMask: true,
	frame:false,
	border:false,
	cls: 'extra-large-cell-grid', 
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
    initComponent: function() {
    	
		this.store =  storeAndModelFactory('SupplierApp.model.NonComplianceSupplier',
                'supplierModel',
                'noncompliancesupplier/searchByCriteria.action', 
                true,
                {
			        query : ''
                },
			    "", 
			    11);
 
        this.columns = [
           {
            text     : 'RFC',
            width: 120,
            dataIndex: 'taxId'
        },{
            text     : SuppAppMsg.suppliersNameSupplier,
            width: 420,
            dataIndex: 'supplierName'
        },{
            text     : SuppAppMsg.complianceSituation,
            width: 400,
            dataIndex: 'status'
        },{
            text     : SuppAppMsg.compliancepostSAT,
            width: 450,
            dataIndex: 'refDate2'
        }];
        
       
        this.tbar = [{
			xtype: 'textfield',
            fieldLabel:SuppAppMsg.complianceSearchText,
            id: 'nonComplianceSearch',
            itemId: 'nonComplianceSearch',
            name:'nonComplianceSearch',
            width:400,
            labelWidth:110,
            margin:'20 20 20 20'
		},{
       		xtype:'button',
            text: SuppAppMsg.suppliersSearch,
            iconCls: 'icon-doSearch',
            action:'nonComplianceSearchBtn',
            margin:'20 20 20 20'
		}];

		this.bbar = Ext.create('Ext.PagingToolbar', {
		store: this.store,
		displayInfo: true
		});
      
        this.callParent(arguments);
    }
});