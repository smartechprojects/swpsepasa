Ext.define('SupplierApp.view.notice.NoticeGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.noticeGrid',
	loadMask : true,
	frame : false,
	border : false,
	cls: 'extra-large-cell-grid', 
	//style :'border: solid #ccc 1px',
	store : 'Notice',
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
			text : 'Id Notice',
			width : 100,
			hidden:true,
			dataIndex : 'id'
		}, {
			text : 'Id Notice',
			width : 100,
			dataIndex : 'idNotice'
		},{
			text : 'Titulo de aviso',
			width : 200,
			dataIndex : 'noticeTitle'
		},{
			text : SuppAppMsg.usersUser,
			width : 120,
			dataIndex : 'createdBy'
		},{
            xtype: 'actioncolumn', 
            width: 80,
            header: 'Proveedores',
            align: 'center',
			name : 'openSuppNotice',
			itemId : 'openSuppNotice',
            style: 'text-align:center;',
            items: [
            	{
            	icon:'resources/images/notepad.png',
          	     iconCls: 'increaseSize',
            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  /*if((r.data.approvalNotes == null || r.data.approvalNotes == '') &&
              			(r.data.rejectNotes == null || r.data.rejectNotes == '')) {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }*/
              	  },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]
        },{
			text : SuppAppMsg.usersActivo,
			width : 50,
			dataIndex : 'enabled'
		}];

		this.bbar = Ext.create('Ext.PagingToolbar', {
			store : 'Notice',
			displayInfo : true,
			height : 55
		});

		this.tbar = [ {
			name : 'searchNotices',
			itemId : 'searchNotices',
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