Ext.define('SupplierApp.view.users.UsersGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.usersGrid',
	loadMask : true,
	frame : false,
	border : false,
	cls: 'extra-large-cell-grid', 
	//style :'border: solid #ccc 1px',
	store : 'Users',
	scroll : true,
	viewConfig : {
		stripeRows : true,
		style : {
			overflow : 'auto',
			overflowX : 'hidden'
		}
	},
	initComponent : function() {

		this.columns = [ {
			text : SuppAppMsg.suppliersName,
			width : 200,
			dataIndex : 'name'
		},{
			text : SuppAppMsg.usersUser,
			width : 120,
			dataIndex : 'userName'
		}, {
			text : SuppAppMsg.usersRole,
			width : 100,
			dataIndex : 'userRole.id',
			renderer:function (value,metaData,record,row,col,store,gridView){
           	 return record.raw.userRole.strValue1;
			 }
		}, {
			text : SuppAppMsg.usersUserType,
			width : 150,
			dataIndex : 'userType.id',
			renderer:function (value,metaData,record,row,col,store,gridView){
           	 return record.raw.userType.strValue1;
			 }
		},{
			text : SuppAppMsg.usersActivo,
			width : 50,
			dataIndex : 'enabled'
		},{
			hidden:true,
			hideable: false,
			width : 50,
			dataIndex : 'openOrders'
		},{
			hidden:true,
			hideable: false,
			width : 50,
			dataIndex : 'logged'
		},{
			hidden:true,
			hideable: false,
			width : 50,
			dataIndex : 'agreementAccept'
		}  ];

		this.bbar = Ext.create('Ext.PagingToolbar', {
			store : 'Users',
			displayInfo : true,
			height : 55
		});

		this.tbar = [ {
			name : 'searchUsers',
			itemId : 'searchUsers',
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