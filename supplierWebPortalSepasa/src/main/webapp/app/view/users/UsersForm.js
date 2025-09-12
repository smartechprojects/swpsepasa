Ext.define('SupplierApp.view.users.UsersForm', {
	extend : 'Ext.form.Panel',
	alias : 'widget.usersForm',
	border : false,
	frame : false,
	style: 'border: solid #ccc 1px',
	autoScroll : true,
	initComponent : function() {
		this.items = [ {
			xtype : 'container',
			layout : 'vbox',
			margin : '15 15 0 10',
			style : 'border-bottom: 1px dotted #fff;padding-bottom:10px',
			defaults : {
				labelWidth : 150,
				margin : '5 15 5 0',
				xtype : 'textfield',
				labelAlign: 'left'
			},
			items : [ {
				xtype : 'hidden',
				name : 'id'
			}, {
				fieldLabel : SuppAppMsg.usersUserId,
				name : 'userName',
				id : 'usersFormUserName',
				width : 300,
				allowBlank : false
			}, {
				fieldLabel : SuppAppMsg.usersFullName,
				name : 'name',
				id : 'usersFormName',
				width : 600,
				allowBlank : false
			}, {
				fieldLabel : SuppAppMsg.usersEmail,
				name : 'email',
				id : 'usersFormEmail',
				vtype : 'email',
				width : 400,
				allowBlank : false,
	            listeners:{
					change: function(field, newValue, oldValue){
						field.setValue(newValue.toLowerCase());
					}
				}
			}, {
				fieldLabel : SuppAppMsg.usersPass,
				name : 'password',
				width : 300,
				//enforceMaxLength :8,
				minLengthText : SuppAppMsg.minLength + '{0}</br><br>',
				maxLengthText : 'El tamaño maximo para este campo es de {0}</br><br>',
				minLength : 8,
				maxLength : 20,
				//vtype:'secPass', 
				inputType: 'password',
				allowBlank : false,
				readonly:true,
				validator: function(v) {
					var specChar = ["&","!","@","#","$","|","%",":","_","=","."];
					   
					   var existChar = false;
					   
					   for(var i = 0;i<specChar.length;i++){
							var item = specChar[i];
							
							if(v.includes(item)){
								existChar = true;
								break;
							}
						}
					   
					   if(existChar){
						   return true;
					   }else return "Debe manejar 1 o mas caracteres especiales (&,!,@,#,$,|,%,:,_,=,.).<br><br>";
					   //return /[A-Za-z &]/.test(v)?true:"Solo permitido espacios y &";
					}
			}, {
				xtype : 'combo',
				fieldLabel : SuppAppMsg.usersRoleAuth,
				id : 'usersRoleCombo',
				name : 'userRole.id',
				store : getUDCStore('ROLES', '', '', ''),
				triggerAction : 'all',
				valueField : 'id',
				displayField : 'strValue1',
				emptyText : 'Selecciona...',
				width : 350,
				allowBlank : false
			}, {
				xtype : 'combo',
				fieldLabel : SuppAppMsg.usersUserType,
				id : 'userTypeCombo',
				name : 'userType.id',
				store : getUDCStore('USERTYPE', '', '', ''),
				triggerAction : 'all',
				valueField : 'id',
				displayField : 'strValue1',
				emptyText : 'Selecciona...',
				width : 399,
				allowBlank : false
			},{
				xtype : 'checkbox',
				fieldLabel : SuppAppMsg.usersActivo,
				name : 'enabled',
				width : 300,
				checked: true
			},{
				xtype : 'checkbox',
				fieldLabel : SuppAppMsg.usersSystem,
				name : 'logged',
				width : 400,
				readOnly:true
			},{
				xtype : 'checkbox',
				fieldLabel : 'Ha aceptado el acuerdo',
				name : 'agreementAccept',
				width : 400,
				readOnly: true,
				hidden : true
			}, {
				name : 'notes',
				width : 600,
				hidden : true
			},{
				xtype : 'checkbox',
				fieldLabel : SuppAppMsg.usersExepAcces,
				name : 'exepAccesRule',
				width : 400
			}

			]
		} ];

		this.tbar = [ {
			iconCls : 'icon-save',
			itemId : 'saveUsers',
			id : 'saveUsers',
			text : SuppAppMsg.usersSave,
			action : 'saveUsers'
		}, {
			iconCls : 'icon-delete',
			itemId : 'deleteUsers',
			id : 'deleteUsers',
			text : 'Eliminar',
			action : 'deleteUsers',
			disabled : true,
			hidden:true
		}, '-', {
			iconCls : 'icon-accept',
			itemId : 'updateUsers',
			id : 'updateUsers',
			text : SuppAppMsg.usersUpdate,
			action : 'updateUsers',
			disabled : true
		}, '-', {
			iconCls : 'icon-add',
			itemId : 'usersNew',
			text : SuppAppMsg.usersNew,
			action : 'usersNew',
			margin : '5 0 10 0'
		} ];
		this.callParent(arguments);
	}

});