Ext.Ajax.on('requestcomplete', function (conn, response, options) {
	try{
        eval( "(" + response.responseText + ')' );
    }catch(e){
    	var loginIdent = "loginSupplier";
    	var str =response.responseText.trim()
        if(str.includes(loginIdent)){
        	Ext.Msg.show({
        		   title:'Aviso',
        		   msg: 'Su sesión ha expirado. Será dirigido a la pagina de inicio para que inicie una nueva sesión.',
        		   buttons: Ext.Msg.YES,
        		   buttonText: {
                       yes: 'Enterado',
                   },
        		   fn: function (buttonValue, inputText, showConfig) {
        			   location.href = "j_spring_security_logout";
        		   },
        		   animEl: 'elId'
        		});
        	
        }
    }
   
});

Ext.apply(Ext.tip.QuickTipManager.getQuickTip(), {
	dismissDelay : 15000 
});
Ext.Loader.setConfig({
	enabled : true
});


Ext.apply(Ext.form.VTypes,{
	secPass : function ValidaRfc(rfcStr) {
		var strCorrecta;
		strCorrecta = rfcStr;

		var valid = '(?=.*[!@#&$|_=.%])[0-9a-zA-Z!@#&$|_=.%0-9]{8}$';
		var validRfc = new RegExp(valid);
		var matchArray = strCorrecta.match(validRfc);
		if (matchArray == null) {
			return false;
		} else {
			return true;
		}
	},
	secPassText : 'Especificación: Contraseña entre 8 y 20 caracteres. Alfanumérica. Debe manejar 1 o 2 caracteres especiales.',
	secPassMask : /[A-Za-z0-9,Ñ,ñ,&,!,@,#,$,|,%,:,_,=,.]/,
	rfc : function ValidaRfc(rfcStr) {
		var strCorrecta;
		strCorrecta = rfcStr;

		var valid = '[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9][A-Z,0-9][0-9,A-Z]';
		var validRfc = new RegExp(valid);
		var matchArray = strCorrecta.match(validRfc);
		if (matchArray == null) {
			return false;
		} else {
			return true;
		}
	},
	rfcText : 'RFC No valido',
	rfcMask : /[A-Za-z0-9,Ñ,ñ,&,-]/,
	celphone : function(value, field) {
		return value.replace(/[ \-\(\)]/g, '').length == 10;
	},
	daterange : function(val, field) {
		var date = field.parseDate(val);

		if (!date) {
			return false;
		}
		if (field.startDateField
				&& (!this.dateRangeMax || (date.getTime() != this.dateRangeMax
						.getTime()))) {
			var start = field.up('form').down(
					'#' + field.startDateField);
			start.setMaxValue(date);
			start.validate();
			this.dateRangeMax = date;
		} else if (field.endDateField
				&& (!this.dateRangeMin || (date.getTime() != this.dateRangeMin
						.getTime()))) {
			var end = field.up('form').down(
					'#' + field.endDateField);
			end.setMinValue(date);
			end.validate();
			this.dateRangeMin = date;
		}
		return true;
	},
	daterangeText : 'Start date must be less than end date'
});

var udcEnabledFilter = new Ext.util.Filter({
	filterFn : function(rec) {
		return rec.get('booleanValue');
	}
});


var keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuv"
		+ "wxyz0123456789+/" + "=";

function storeFactory(name, url, fields, autoLoad, extraParams, groupField,
		pageSize) {
	return new Ext.data.Store({
		autoLoad : autoLoad,
		model : modelFactory(name, fields),
		groupField : groupField ? groupField : "",
		pageSize : pageSize ? pageSize : "",
		proxy : {
			type : 'ajax',
			url : url,
			extraParams : extraParams,
			reader : {
				type : 'json',
				root : 'data'
			},
			writer : {
				type : 'json',
				writeAllFields : true,
				encode : false
			}
		}
	});
};

function storeAndModelFactory(model, name, url, autoLoad, extraParams, groupField, pageSize) {
	return new Ext.data.Store({
		autoLoad : autoLoad,
		enablePaging: true,
		model : model,
		groupField : groupField ? groupField : "",
		pageSize : pageSize ? pageSize : "",
		proxy : {
			type : 'ajax',
			url : url,
			enablePaging: true,
			extraParams : extraParams,
			reader : {
				type : 'json',
				root : 'data'
			},
			writer : {
				type : 'json',
				writeAllFields : true,
				encode : false
			}
		}
	});
};

function modelNull(v, j) {
	if (v != null) {
		if (v.id != null && v.id != "") {
			return v;
			// {id: v.id,
			// strValue1:v.strValue1};
		} else {
			return v.id;
		}
	} else {
		return "";
	}

}
function modelFactory(name, fields) {
	return Ext.define(name, {
		extend : 'Ext.data.Model',
		fields : fields
	});
};

function nullModel(v, j) {
	return (v != null ? v.id : "");
};

function getUDCStore(udcSystem, udcKey, systemRef, keyRef) {
	return new Ext.data.Store({
		fields : [ 'id', 'udcKey', 'strValue1', 'systemRef', 'keyRef', 'strValue2' ],
		autoLoad : false,
		proxy : {
			type : 'ajax',
			url : 'admin/udc/searchSystemAndKey.action',
			extraParams : {
				query:'',
				udcSystem : udcSystem,
				udcKey : udcKey,
				systemRef : systemRef,
				keyRef : keyRef
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

function getAutoLoadUDCStore(udcSystem, udcKey, systemRef, keyRef) {
	return new Ext.data.Store({
		fields : [ 'id', 'udcKey', 'strValue1', 'systemRef', 'keyRef', 'strValue2', 'booleanValue','disabled'],
		autoLoad : true,
		proxy : {
			type : 'ajax',
			url : 'public/searchSystemAndKey.action',
			extraParams : {
				query:'',
				udcSystem : udcSystem,
				udcKey : udcKey,
				systemRef : systemRef,
				keyRef : keyRef
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

function getAutoLoadUDCStore(udcSystem) {
	return new Ext.data.Store({
		fields : [ 'id', 'udcKey', 'strValue1', 'systemRef', 'keyRef', 'strValue2', 'booleanValue', {name:'CompuestoField', convert: function(value, record) {
		    return record.data.udcKey + '-' + record.data.strValue1;
		}} ],
		autoLoad : true,
		
		proxy : {
			type : 'ajax',
			url : 'public/searchSystem.action',
			extraParams : {
				udcSystem : udcSystem
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

function getSuppliersByFilter(query) {
	return new Ext.data.Store({
		fields : [ 'id', 'addresNumber', 'razonSocial'],
		autoLoad : true,
		proxy : {
			type : 'ajax',
			url : 'supplier/getSuppliersByFilter.action',
			extraParams : {
				query:''
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

function getAutoLoadUDCStoreWithFilterStrValue2(udcSystem, filterBy) {
	var addedRecords=[];
	return new Ext.data.Store({
		fields : [ 'id', 'udcKey', 'strValue1', 'systemRef', 'keyRef', 'strValue2', 'booleanValue' ],
		autoLoad : true,
		proxy : {
			type : 'ajax',
			url : 'public/searchSystemAndKey.action',
			extraParams : {
				query:'',
				udcSystem : udcSystem,
				udcKey : '',
				systemRef : '',
				keyRef : ''
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		},
		filters: [
	         function(item) {
		         if(role === 'ROLE_ADMIN' || role === 'ROLE_SUPPLIER' || role === 'ROLE_REPSE' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT'){
		        	 return true;
		         }else{
			         return item.data.strValue2 === filterBy; //true
		         }
	         }
	    ]
	});	
};

function getAutoLoadUDCStoreByRoleInStrValue2(udcSystem, strValue2) {
	var addedRecords=[];
	return new Ext.data.Store({
		fields : [ 'id', 'udcKey', 'strValue1', 'strValue2', 'systemRef', 'keyRef', 'booleanValue' ],
		autoLoad : true,
		proxy : {
			type : 'ajax',
			url : 'public/searchSystemByRoleInStrValue2.action',
			extraParams : {
				query:'',
				udcSystem : udcSystem,
				udcKey : '',
				strValue1 : '',
				strValue2 : strValue2,
				systemRef : '',
				keyRef : ''
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
	
};

function getCPStore() {
	return new Ext.data.Store({
		fields : [ 'id', 'codigo', 'colonia', 'tipoColonia', 'municipio', 'estado' ],
		autoLoad : false,
		pageSize : 100,
		proxy : {
			type : 'ajax',
			url : 'codigoPostal/view.action',
			extraParams : {
				query : "",
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

function populateObj(record, values) {
	var obj = {}, name;

	record.fields.each(function(field) {
		name = field.name;
		if (field.model) {
			var nestedValues = {};
			var hasValues = false;
			for ( var v in values) {
				if (v.indexOf('.') > 0) {
					var parent = v.substr(0, v.indexOf('.'));
					if (parent == field.name) {
						var key = v.substr(v.indexOf('.') + 1);
						nestedValues[key] = values[v];
						hasValues = true;
					}
				}
			}
			if (hasValues) {
				obj[name] = populateObj(Ext.create(field.model), nestedValues);
			}
		} else if (name in values) {
			obj[name] = values[name];
		}
	});
	return obj;
};

Ext.toggle = function(msgCt, delay) {

	function createBox(t, s) {
		return '<div class="msg"><h3>' + t + '</h3><p>' + s + '</p></div>';
	}

	return {
		msg : function(title, format) {
			if (!msgCt) {
				msgCt = Ext.DomHelper.insertFirst(document.body, {
					id : 'msg-div'
				}, true);
			}
			var s = Ext.String.format.apply(String, Array.prototype.slice.call(
					arguments, 1));
			var m = Ext.DomHelper.append(msgCt, createBox(title, s), true);
			m.hide();
			m.slideIn('t').ghost("t", {
				delay : 2500,
				remove : true
			});
		},

		init : function() {
		}
	};
}();

function openChangePasswordWindow() {
	win = new Ext.create(
			'Ext.Window',
			{
				title : GdsLims.app.messages.lblChangePassword,
				height : 150,
				id : "testWindow",
				width : 400,
				layout : 'fit',
				applyTo : 'logout',
				resizable : false,
				draggable : false,
				modal : true,
				plain : true,
				border : false,
				items : [ {
					xtype : 'panel',
					region : 'center',
					id : 'chPswPanel',
					margins : '0 0 0 0',
					items : [ {
						xtype : 'fieldset',
						margin : '5 0 0 10',
						border : false,
						defaultType : 'textfield',
						items : [
								{
									xtype : 'hidden',
									name : 'userId',
									id : 'idUser',
									value : usrPrf.id
								},
								{
									fieldLabel : GdsLims.app.messages.lblCurrentPassword,
									labelWidth : 120,
									name : 'currentPassword',
									id : 'currentPassword',
									inputType : 'password',
									allowBlank : false
								},
								{
									fieldLabel : GdsLims.app.messages.lblNewPassword,
									labelWidth : 120,
									name : 'newPassword',
									id : 'newPassword',
									inputType : 'password',
									allowBlank : false,
									margin : '10 0 0 0'
								} ]
					} ]
				} ],

				buttons : [ {
					text : GdsLims.app.messages.btnSubmit,
					formBind : true,
					iconCls : 'icon-accept',
					handler : function() {

						var identUser = Ext.getCmp('idUser').getValue();
						var oldPass = Ext.getCmp('currentPassword');
						var newPass = Ext.getCmp('newPassword');

						if (oldPass.getValue() != ""
								&& newPass.getValue() != "") {
							if (oldPass.getValue() != decode64(usrPrf.password)) {
								Ext.toggle
										.msg(
												"<span style='color:red;'>ERROR!</span>",
												'La contraseña actual no conicide. Corrija el valor de su contraseña actual y vuelva a intentarlo.');
								return false;
							}
							if (oldPass.getValue() == newPass.getValue()) {
								Ext.toggle
										.msg(
												"<span style='color:red;'>ERROR!</span>",
												'La nueva contraseña no debe ser igual a la contraseña actual.');
								return false;
							}

							Ext.Ajax
									.request({
										url : 'admin/users/changePassword.action',
										method : 'POST',
										waitMsg : 'Enviando la solicitud...',
										params : {
											userId : identUser,
											currentPassword : oldPass
													.getValue(),
											newPassword : newPass.getValue()
										},
										success : function(response) {
											win.close();
											Ext.toggle
													.msg("Operación realizada",
															'La contraseña ha sido modificada exitosamente.');
										},
										failure : function() {
											Ext.Msg
													.alert('Advertencia!',
															'El servidor a notificado un error no se ha cambiado la contraseña ');
											oldPass.setValue('');
											newPass.setValue('');
										}
									});
						} else {
							Ext.Msg.alert('Error',
									'Los campos no pueden estar vacios');
						}
					}
				} ]
			});
	win.show();
};


// Moficación para llenar combobox y no esten en vacio
// J.camacho
function setComboDefaultValue(comboBox, store) {
	store.on('load', function() {
		this.data.each(function() {
			if (this.data['booleanValue'])
				comboBox.setValue(this.data['id']);
		});
	});
	store.load();
};

function getCategoryJDEList(){
	 var store = Ext.create('Ext.data.Store', {
       fields: ['categoriaJDE'],
       data: [{ 'categoriaJDE': 'NON FOOD'},
              { 'categoriaJDE': 'FOOD'}
              ]
   });
	 return store;
};

function getBancoList(){
	 var store = Ext.create('Ext.data.Store', {
      fields: ['nombreBanco'],
      data: [{ 'nombreBanco': 'ABC Capital '},
             { 'nombreBanco': 'American Express Bank (México)'},
             { 'nombreBanco': 'Banca Afirme'},
             { 'nombreBanco': 'banca Mifel'},
             { 'nombreBanco': 'Banco Actinver'},
             { 'nombreBanco': 'Banco Ahorro Famsa'},
             { 'nombreBanco': 'Banco Autofin México'},
             { 'nombreBanco': 'Banco Azteca'},
             { 'nombreBanco': 'Banco Bancrea'},
             { 'nombreBanco': 'Banco Base'},
             { 'nombreBanco': 'Banco Compartamos'},
             { 'nombreBanco': 'Banco Credit Suisse (México)'},
             { 'nombreBanco': 'Banco del Bajio'},
             { 'nombreBanco': 'Banco Forjadores'},
             { 'nombreBanco': 'Banco Inbursa'},
             { 'nombreBanco': 'Banco Inmobiliario Mexicano'},
             { 'nombreBanco': 'Banco Interacciones'},
             { 'nombreBanco': 'Banco Invex'},
             { 'nombreBanco': 'Banco JP Morgan'},
             { 'nombreBanco': 'Banco Mercantil del Norte (Banorte)'},
             { 'nombreBanco': 'Banco Monex'},
             { 'nombreBanco': 'Banco Multiva'},
             { 'nombreBanco': 'Banco Nacional de México (Banamex)'},
             { 'nombreBanco': 'Banco Pagatodo'},
             { 'nombreBanco': 'Banco Regional de Monterrey'},
             { 'nombreBanco': 'Banco Santander (México)'},
             { 'nombreBanco': 'Banco Ve Por Mas'},
             { 'nombreBanco': 'Banco Wal-Mart de México'},
             { 'nombreBanco': 'Bancoppel'},
             { 'nombreBanco': 'Bank of America México'},
             { 'nombreBanco': 'Bank of Tokyo-Mitsubishi UFJ (México)'},
             { 'nombreBanco': 'Bankaool'},
             { 'nombreBanco': 'Bansi'},
             { 'nombreBanco': 'Barclays Bank México'},
             { 'nombreBanco': 'BBVA Bancomer'},
             { 'nombreBanco': 'CiBanco'},
             { 'nombreBanco': 'ConsuBanco'},
             { 'nombreBanco': 'Deutsche Bank México'},
             { 'nombreBanco': 'Fundación Dondé Banco'},
             { 'nombreBanco': 'HSBC México'},
             { 'nombreBanco': 'Intercam Banco'},
             { 'nombreBanco': 'Investa Bank'},
             { 'nombreBanco': 'Scotiabank Inverlat'},
             { 'nombreBanco': 'UBS Bank México'},
             { 'nombreBanco': 'Volkswagen Bank'}
             ]
  });
	 return store;
};

function getFormaPagoList(){
	 var store = Ext.create('Ext.data.Store', {
     fields: ['formaPago'],
     data: [{ 'formaPago': 'Efectivo'},
            { 'formaPago': 'Cheque nominativo'},
            { 'formaPago': 'Transferencia electrónica de fondos'},
            { 'formaPago': 'Tarjeta de crédito'},
            { 'formaPago': 'Monedero electrónico'},
            { 'formaPago': 'Dinero electrónico'},
            { 'formaPago': 'Vales de despensa'},
            { 'formaPago': 'Tarjeta de débito'},
            { 'formaPago': 'Tarjeta de servicio'},
            { 'formaPago': 'Otros'}
            ]
 });
	 return store;
};

function getTipoProductoList(){
	 var store = Ext.create('Ext.data.Store', {
    fields: ['tipoProductoServicio'],
    data: [{ 'tipoProductoServicio': 'Tipo001'},
           { 'tipoProductoServicio': 'Tipo002'},
           { 'tipoProductoServicio': 'Tipo003'},
           { 'tipoProductoServicio': 'Tipo004'},
           { 'tipoProductoServicio': 'Tipo005'},
           { 'tipoProductoServicio': 'Tipo006'},
           { 'tipoProductoServicio': 'Tipo007'},
           { 'tipoProductoServicio': 'Tipo008'},
           { 'tipoProductoServicio': 'Tipo009'},
           { 'tipoProductoServicio': 'Tipo0010'}
           ]
	 });
	 return store;
};

function getRiesgoList(){
	 var store = Ext.create('Ext.data.Store', {
   fields: ['riesgoCategoria'],
   data: [{ 'riesgoCategoria': 'BAJO'},
          { 'riesgoCategoria': 'MEDIO'},
          { 'riesgoCategoria': 'ALTO'}
          ]
	 });
	 return store;
};

function getTasaIvaList(){
	 var store = Ext.create('Ext.data.Store', {
  fields: ['tasaIva'],
  data: [{ 'tasaIva': 'SI'},
         { 'tasaIva': 'N-A'}
         ]
	 });
	 return store;
};


function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) 
        month = '0' + month;
    if (day.length < 2) 
        day = '0' + day;

    return [year, month, day].join('-');
}


function deleteDocument (docId) {
	Ext.MessageBox.show({
		title : 'Eliminación de documentos',
		msg : 'Desea eliminar el documento?',
		buttons : Ext.MessageBox.YESNO,
		width:500,
		buttonText : {
			yes : "Aceptar",
			no : "Salir"
		},
		fn : function(btn, text) {
			if (btn === 'yes') {
				var box = Ext.MessageBox.wait('Procesando. Espere unos segundos', 'Ejecución');
		    	Ext.Ajax.request({
					url : 'documents/delete.action',
					method : 'GET',
						params : {
							id:docId
						},
						success : function(response,opts) {
							var resp = Ext.decode(response.responseText);
							store.load();
							box.hide();
						},
						failure : function() {
							box.hide();
						}
					});
				
				
			}
		}
	});
	
}

function getUsersByRoleExcludeStore(role) {
	return new Ext.data.Store({
		fields : [ 'id', 'userName', 'name', 'email' ],
		autoLoad : true,
		pageSize : 100,
		proxy : {
			type : 'ajax',
			url : 'admin/users/searchByRoleExclude.action',
			extraParams : {
				query : '',
				role:role
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

function getUsersByApprovalStepStore(step) {
	return new Ext.data.Store({
		fields : [ 'id', 'userName', 'name', 'email' ],
		autoLoad : true,
		pageSize : 100,
		proxy : {
			type : 'ajax',
			url : 'admin/users/searchByApprovalStep.action',
			extraParams : {
				query : '',
				step:step
			},
			reader : {
				root : 'data',
				totalProperty : 'total',
				type : 'json'
			}
		}
	});
};

Date.prototype.addHours = function(h) {
	  this.setTime(this.getTime() + (h*60*60*1000));
	  return this;
}

function wait(ms){
	   var start = new Date().getTime();
	   var end = start;
	   while(end < start + ms) {
	     end = new Date().getTime();
	  }
}


