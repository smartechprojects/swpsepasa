Ext.define('SupplierApp.view.token.TokenGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.tokenGrid',
    loadMask: true,
	frame:false,
	border:true,
    forceFit: true,
	cls: 'extra-large-cell-grid', 
    store:'AccessTokenRegister',
	scroll : false,
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' }
	},
	
    initComponent: function() {
 
        this.columns = [
        	{
                hidden:true,
                dataIndex: 'id'
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Proveedor':'Supplier',
                dataIndex: 'registerName',
                width: 100
            },{
                text     : 'RFC/Tax ID',
                dataIndex: 'rfc',
                width: 85
            },{
                text     : 'Tipo de busqueda',
                dataIndex: 'searchType',
                width: 85
            },{
                text     : 'Email',
                dataIndex: 'email',
                width: 85
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Creado por':'Created by',
                dataIndex: 'createdBy',
                width: 45
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Actualizado por':'Updated by',
                dataIndex: 'updatedBy',
                width: 45
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Activo':'Enabled',
                dataIndex: 'enabled',
                width: 30
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Asignado':'Assigned',
                dataIndex: 'assigned',
                width: 30
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Fecha de Registro':'Creation Date',
                dataIndex: 'creationDate',
                width: 60,
                renderer : Ext.util.Format.dateRenderer("d-m-Y")
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Fecha Actualización':'Update Date',
                dataIndex: 'updatedDate',
                width: 60,
                renderer : Ext.util.Format.dateRenderer("d-m-Y")
            },{
                text     : window.navigator.language.startsWith("es", 0)? 'Fecha Expiración':'Expiration Date',
                dataIndex: 'expirationDate',
                width: 60,
                renderer : Ext.util.Format.dateRenderer("d-m-Y")
            }];
      
        this.bbar = Ext.create('Ext.PagingToolbar', {
			store : 'AccessTokenRegister',
			displayInfo : true,
			height : 30
		});

		this.tbar = [ {
			name : 'searchAccessToken',
			itemId : 'searchAccessToken',
			emptyText : SuppAppMsg.suppliersSearch,
			xtype : 'trigger',
			width : 400,
			margin: '5 0 10 0',
			triggerCls : 'x-form-search-trigger',
			onTriggerClick : function(e) {
				this.fireEvent("ontriggerclick", this, event);
			},
			enableKeyEvents : true,
			listeners : {
				specialkey : function(field, e) {
					if (e.ENTER === e.getKey()) {
						field.onTriggerClick();
					}
				}
			}
		} ];
		
        this.callParent(arguments);
    }
});