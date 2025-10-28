Ext.define('SupplierApp.view.supplier.SupplierForm', {
	extend : 'Ext.form.Panel',
	alias : 'widget.supplierForm',
	border : false,
	id:'supplierFormId',
	frame : false,
	style: 'border: solid #ccc 1px',
	autoScroll : true,
    getInvalidFields: function() {
        var invalidFields = [];
        Ext.suspendLayouts();
        this.form.getFields().filterBy(function(field) {
            if (field.validate()) return;
            invalidFields.push(field);
        });
        Ext.resumeLayouts(true);
        return invalidFields;
    },
	initComponent : function() {
		
		
		var colStore = Ext.create('Ext.data.Store', {
		    fields: ['id_', 'name'],
		    data: [{
		            'id_': 'Sin datos',
		            'name': 'Sin datos'
		        }
		    ]

		});
		
		
		this.items = [{
						xtype : 'hidden',
						name : 'id'
					},{
						xtype : 'hidden',
						name : 'approvalStatus'
					},{
						xtype : 'hidden',
						name : 'approvalStep'
					},{
						xtype : 'hidden',
						name : 'steps',
						id:'steps',
						value:2
					},{
						xtype : 'hidden',
						name : 'ukuid'
					},{
					xtype: 'container',
					margin:'15 0 0 10',
					layout : {
		                  type :'table',
		                  columns : 3,
		                  tableAttrs: {
		                     style: {
		                        width: '95%'
		                     }
		                  }               
		             },
		            autoHeight:true, 
		            autoWidth:true, 
					cls: 'valign_class',
					defaults : {
						labelWidth : 130,
						xtype : 'textfield',
						margin: '15 0 0 10',
						width : 320,
						align: 'stretch',
						//fieldStyle: 'padding-bottom:5px;font-size:18px;vertical-align:top;border:none;background:transparent;color:black;font-weight:bold',
						//readOnly:true
					},
			        items:[{
						name : 'ticketId',
						hidden: true,
						id:'ticketId',
						value:0,
						readOnly:true,
						colspan:3,
						fieldLabel:'Num. Ticket',
						margin: '0 0 20 10',
						fieldStyle: 'border:none;background-color: #ddd; background-image: none;',
						 tip: 'Este campo está protegido',
						    listeners: {
						      render: function(c) {
						        Ext.create('Ext.tip.ToolTip', {
						          target: c.getEl(),
						          html: c.tip 
						        });
						      }
						    }
					},{
						   fieldLabel : SuppAppMsg.supplierForm1,
						   name : 'fechaSolicitud',
						   id:'fechaSolicitud',
						   width:250,
						   xtype:'datefield',
						   format: 'd-m-Y',
						   margin:'0 0 20 10',
						   value: new Date(),
						   readOnly:true,
						   hidden : role == 'ROLE_SUPPLIER'?true:false,
						   fieldStyle: 'border:none;background-color: #ddd; background-image: none;'
						   },{
							xtype : 'displayfield',
							value : SuppAppMsg.supplierForm2,
							height:20,
							id: 'formatoSolicitud',
							margin: '50 0 10 10',
							colspan:2,
							hidden : role == 'ROLE_SUPPLIER'?true:false,
							fieldStyle: 'font-weight:bold;font-size:20px;'
						},{
								xtype:'container',
				                colspan:3,
				                hidden : role == 'ROLE_SUPPLIER' || role=='ANONYMOUS'? true:false,
								layout: {
								    type: 'hbox',
								    pack: 'start',
								    align: 'stretch'
								},
								items: [{
									xtype:'textfield',
									labelWidth:130,
									fieldLabel : SuppAppMsg.approvalTicket,
									name : 'ticketForSearch',
									id : 'ticketForSearch',
									itemId : 'ticketForSearch',
									labelAlign:'left',
									margin:'0 5 0 0',
									width:250,
									colspan:2,
									readOnly:true,
									tip: SuppAppMsg.supplierForm3,
								    listeners: {
								      render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      }
								    }
							  },{
									xtype: 'button',
									width:60,
									//hidden : role == 'ROLE_SUPPLIER'?true:false,
									hidden:true,
									text : SuppAppMsg.suppliersSearch,
									action : 'searchTicket',
									id : 'searchTicket',
									maring:'0 0 0 0'
								}]
							},{
								fieldLabel : 'Ticket de cambios',
								name : 'changeTicket',
								id : 'changeTicket',
								labelAlign:'center',
								margin:'10 0 0 10',
								colspan:3,
								readOnly:true,
								//allowBlank:true,
								//hidden:true,
								//maxLength : 8,
								//enforceMaxLength : true,
								maskRe: /[0-9]/,
								hidden:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? true:false,
								//allowBlank:false
								//hidden:role=='ANONYMOUS'?true:false,
								//fieldStyle: 'border:none;background-color: #ddd; background-image: none;',
							    //tip: 'Este campo está protegido',							    	
							},{
								fieldLabel : SuppAppMsg.suppliersNumber,
								name : 'addresNumber',
								id : 'addresNumber',
								labelAlign:'center',
								margin:'10 0 0 10',
								colspan:3,
								readOnly:true,
								//allowBlank:true,
								//hidden:true,
								maxLength : 8,
								enforceMaxLength : true,
								maskRe: /[0-9]/,
								//allowBlank:false
								hidden:role=='ANONYMOUS'?true:false,
								//fieldStyle: 'border:none;background-color: #ddd; background-image: none;',
							    //tip: 'Este campo está protegido',							    	
							},{					                					                						    							    	
								xtype: 'combobox',
				                name: 'country',
				                id: 'country',
				                fieldLabel: SuppAppMsg.purchaseTitle15 + '*',
				                typeAhead: true,
				                typeAheadDelay: 100,
				                allowBlank:false,
				                margin:'10 0 0 10',
				                minChars: 1,
				                colspan:3,
				                queryMode: 'local',
				                //forceSelection: true,
				                store : getAutoLoadUDCStore('COUNTRY', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width:400,
				                readOnly:role =='ROLE_SUPPLIER' ?true:false,
				                editable: false,
							    listeners: {
							    	select: function (comboBox, records, eOpts) {
							    		
							    		var countryCode = records[0].data.udcKey;
							    		if(countryCode != 'MX'){
							    			var taxId = Ext.getCmp('supRfc').getValue();
							    			if(taxId != ""){
							    				Ext.getCmp('taxId').setValue(taxId);
							    			}
							    			Ext.getCmp('supRfc').setValue(null);
							    			
							    			Ext.getCmp('taxId').show();
							    			
							    			Ext.getCmp('supRfc').hide();
							    			
							    			/*Ext.getCmp('taxId').setValue('');
							    			Ext.getCmp('taxId').allowBlank=false;
							    			Ext.getCmp('taxId').show();
							    			
							    			Ext.getCmp('supRfc').hide();
							    			Ext.getCmp('supRfc').setValue(null);
							    			Ext.getCmp('supRfc').allowBlank=true;*/
							    			//Ext.getCmp('glClass').setValue('200');
							    			Ext.getCmp('documentContainerForeingResidence').show();
							    			
							    			Ext.getCmp('searchCP').hide();
							    			
							    			Ext.getCmp('coloniaEXT').setValue('');
							    			Ext.getCmp('coloniaEXT').allowBlank=false;
							    			Ext.getCmp('coloniaEXT').show();
							    			
							    			Ext.getCmp('fldColonia').hide();
							    			Ext.getCmp('fldColonia').setValue(null);
							    			Ext.getCmp('fldColonia').allowBlank=true;
							    			Ext.getCmp('fldMunicipio').hide();
											Ext.getCmp('fldMunicipio').allowBlank=true;
							    			
							    			Ext.getCmp('codigoPostal').allowBlank=true;
							    		}else{
							    			var taxId = Ext.getCmp('taxId').getValue();
							    			if(taxId != ""){
							    				Ext.getCmp('supRfc').setValue(taxId);
							    			}
							    			Ext.getCmp('taxId').setValue(null);
							    			
							    			Ext.getCmp('supRfc').show();
							    			
							    			Ext.getCmp('taxId').hide();
							    			
							    			/*Ext.getCmp('supRfc').show();
							    			Ext.getCmp('supRfc').setValue('');
							    			Ext.getCmp('supRfc').allowBlank=false;
							    			
							    			Ext.getCmp('taxId').hide();
							    			Ext.getCmp('taxId').setValue(null);
							    			Ext.getCmp('taxId').allowBlank=true;*/
							    			//Ext.getCmp('glClass').setValue('100');
							    			Ext.getCmp('documentContainerForeingResidence').hide();
							    			
							    			Ext.getCmp('searchCP').show();
							    			
							    			Ext.getCmp('fldColonia').show();
							    			Ext.getCmp('fldColonia').setValue('');
							    			Ext.getCmp('fldColonia').allowBlank=false;
							    			
							    			Ext.getCmp('coloniaEXT').hide();
							    			Ext.getCmp('coloniaEXT').setValue(null);
							    			Ext.getCmp('coloniaEXT').allowBlank=true;
							    			Ext.getCmp('codigoPostal').allowBlank=false;
							    			
							    			Ext.getCmp('fldMunicipio').show();
											Ext.getCmp('fldMunicipio').allowBlank=false;
							    		}
							    	}
							    }
							},{
							xtype : 'displayfield',
							value : SuppAppMsg.supplierForm4,
							height:20,
							margin: '50 0 0 10',
							colspan:3,
							fieldStyle: 'font-weight:bold'
							},{
						   fieldLabel : SuppAppMsg.supplierForm5,
						   name : 'razonSocial', 
						   itemId : 'razonSocial',
						   id:'razonSocial',
						   //maskRe: /[A-Za-z &]/,  
						   //stripCharsRe: /[^A-Za-z &]/,
						   maskRe: /[A-Za-z\d &]/,
					       stripCharsRe: /[^A-Za-z\d &]/,
						   margin:'10 0 0 10',
						   width : 500,
						   maxLength : 40,
						   enforceMaxLength : true,
						   listeners:{
								change: function(field, newValue, oldValue){
									field.setValue(newValue.toUpperCase());
								}
							},
							allowBlank:false,
							readOnly:role=='ANONYMOUS'?false:true,
							/*regex: /[A-Za-z]/,
							regexText: " ",
							validator: function(v) {
							   debugger;
							   if(/[A-Za-z &]/.test(v)){
								   return true;
							   }else return false;
							   //return /[A-Za-z &]/.test(v)?true:"Solo permitido espacios y &";
							}*/
						   },{
								fieldLabel : SuppAppMsg.supplierForm6,
								name : 'rfc',
								id:'supRfc',
								colspan:2,
								maskRe: /[A-Za-z&\d]/,
								stripCharsRe: /[^A-Za-z&\d]/,
							    vtype:'rfc',
							    maxLength : 13,
								allowBlank:false,
								readOnly:true,
							    listeners:{
									change: function(field, newValue, oldValue){
										/*if(typeof newValue !== 'undefined' && typeof oldValue !== 'undefined'){
											if(newValue.toUpperCase() != oldValue.toUpperCase()){
												Ext.getCmp('hrefFileList').setValue('');
												Ext.getCmp('rfcDocument').setValue('');
												Ext.getCmp('domDocument').setValue('');
												Ext.getCmp('edoDocument').setValue('');
												Ext.getCmp('identDocument').setValue('');
												Ext.getCmp('actaConstitutiva').setValue('');
												Ext.getCmp('rpcDocument').setValue('');
												Ext.getCmp('legalExistence').setValue('');
												Ext.getCmp('foreingResidence').setValue('');
												//Ext.getCmp('identDocument').setValue('');
											}
										}*/
										field.setValue(newValue.toUpperCase());
									}
							    },
							    checkChangeEvents:['change'],
								enforceMaxLength : true
						    },{
								fieldLabel : 'TaxId*',
								name : 'taxId',
								id:'taxId',
								allowBlank:true,
								maxLength : 20,
								maskRe: /[A-Za-z&\d]/,
								stripCharsRe: /[^A-Za-z&\d]/,
								colspan:3,
								readOnly:true,
							    listeners:{
									change: function(field, newValue, oldValue){
										/*if(typeof newValue !== 'undefined' && typeof oldValue !== 'undefined'){
											if(newValue.toUpperCase() != oldValue.toUpperCase()){
												Ext.getCmp('hrefFileList').setValue('');
												Ext.getCmp('rfcDocument').setValue('');
												Ext.getCmp('domDocument').setValue('');
												Ext.getCmp('edoDocument').setValue('');
												Ext.getCmp('identDocument').setValue('');
												Ext.getCmp('actaConstitutiva').setValue('');
												Ext.getCmp('rpcDocument').setValue('');
												Ext.getCmp('legalExistence').setValue('');
												Ext.getCmp('foreingResidence').setValue('');
												//Ext.getCmp('identDocument').setValue('');
											}
										}*/
										field.setValue(newValue.toUpperCase());
									}
								},
								enforceMaxLength : true
							 },{
								   fieldLabel : 'Regimen fiscal',
								   name : 'regimenFiscal', 
								   id:'regimenFiscal',
								   //maskRe: /[A-Za-z &]/,  
								   //stripCharsRe: /[^A-Za-z &]/,
								   //maskRe: /[A-Za-z\d &]/,
							       stripCharsRe: /[^A-Za-z\d &]/,
								   margin:'10 0 0 10',
								   width : 500,
								   maxLength : 20,
								   colspan:1,
								   enforceMaxLength : true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
									allowBlank:false,
									readOnly:role=='ANONYMOUS' || role=='ROLE_SUPPLIER' ?false:true,
									/*regex: /[A-Za-z]/,
									regexText: " ",
									validator: function(v) {
									   debugger;
									   if(/[A-Za-z &]/.test(v)){
										   return true;
									   }else return false;
									   //return /[A-Za-z &]/.test(v)?true:"Solo permitido espacios y &";
									}*/
							},{
								   fieldLabel : 'Codigo tributario',
								   name : 'codigoTriburario', 
								   id:'codigoTriburario',
								   //maskRe: /[A-Za-z &]/,  
								   //stripCharsRe: /[^A-Za-z &]/,
								   //maskRe: /[A-Za-z\d &]/,
							       stripCharsRe: /[^A-Za-z\d &]/,
								   margin:'10 0 0 10',
								   width : 500,
								   maxLength : 20,
								   colspan:3,
								   enforceMaxLength : true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
									allowBlank:false,
									readOnly:role=='ANONYMOUS' || role=='ROLE_SUPPLIER' ?false:true,
									/*regex: /[A-Za-z]/,
									regexText: " ",
									validator: function(v) {
									   debugger;
									   if(/[A-Za-z &]/.test(v)){
										   return true;
									   }else return false;
									   //return /[A-Za-z &]/.test(v)?true:"Solo permitido espacios y &";
									}*/
							},{
								fieldLabel : SuppAppMsg.supplierForm7,
								name : 'emailSupplier',
								id : 'emailSupplier',
								width:450,
								allowBlank:false,
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								readOnly:role=='ROLE_ADMIN' || role=='ANONYMOUS' || role=='ROLE_SUPPLIER' ?false:true,
								//tip: SuppAppMsg.supplierForm50,
								/*regex: /\w@\w/,
								regexText: " ",
							    validator: function(v) {
							    	var coma = v.indexOf(",");
							    	var espacio = v.indexOf(" ");
							    	
							    	if(coma != -1 || espacio != -1){
							    		return SuppAppMsg.supplierForm52 + "<br>";
							    	}else{
							    		return /\w@\w/.test(v)?true:SuppAppMsg.supplierForm50;
							    	}
							        
							    },*/
					            listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
						   },{
								fieldLabel : 'Search Type',
								name : 'searchType',
								id : 'searchType',
								xtype: 'combobox',
								typeAhead: true,
								editable: false,
				                typeAheadDelay: 100,
				                allowBlank:false,
				                minChars: 1,
				                queryMode: 'local',
				                //forceSelection: true,
								store : getAutoLoadUDCStore('SEARCHTYPE', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width : 250,
								colspan:3
						},{
								fieldLabel : SuppAppMsg.supplierForm8,
								name : 'fisicaMoral',
								id : 'fisicaMoral',
								xtype: 'combobox',
								typeAhead: true,
				                typeAheadDelay: 100,
				                allowBlank:false,
				                minChars: 1,
				                queryMode: 'local',
				                //forceSelection: true,
								store : getAutoLoadUDCStore('CONTRIB', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width : 250,
				                editable: false,
								colspan:3,
								listeners: {
							    	select: function (comboBox, records, eOpts) {
							    		var contrib = records[0].data.udcKey;
							    		if(contrib != 'F'){
							    			Ext.getCmp('REPRESENTE_LEGAL').show();
							    			Ext.getCmp('tipoIdentificacion').allowBlank=false;
							    			Ext.getCmp('tipoIdentificacion').show();
							    			Ext.getCmp('numeroIdentificacion').allowBlank=false;
							    			Ext.getCmp('numeroIdentificacion').show();
							    			Ext.getCmp('nombreRL').allowBlank=false;
							    			Ext.getCmp('nombreRL').show();
							    			Ext.getCmp('apellidoPaternoRL').allowBlank=false;
							    			Ext.getCmp('apellidoPaternoRL').show();
							    			Ext.getCmp('apellidoMaternoRL').show();
							    			//Ext.getCmp('catCode27').setValue('85');
							    			Ext.getCmp('curp').hide();
							    			Ext.getCmp('curp').allowBlank=true;
							    		}else{
							    			Ext.getCmp('REPRESENTE_LEGAL').hide();
							    			Ext.getCmp('tipoIdentificacion').allowBlank=true;
							    			Ext.getCmp('tipoIdentificacion').hide();
							    			Ext.getCmp('numeroIdentificacion').allowBlank=true;
							    			Ext.getCmp('numeroIdentificacion').hide();
							    			Ext.getCmp('nombreRL').allowBlank=true;
							    			Ext.getCmp('nombreRL').hide();
							    			Ext.getCmp('apellidoPaternoRL').allowBlank=true;
							    			Ext.getCmp('apellidoPaternoRL').hide();
							    			Ext.getCmp('apellidoMaternoRL').hide();
							    			//Ext.getCmp('catCode27').setValue('03');
							    			Ext.getCmp('curp').show();
							    			Ext.getCmp('curp').allowBlank=false;
							    		}
							    	}
							    }
							},{
								   fieldLabel : 'CURP',
								   name : 'curp', 
								   id:'curp',
								   //maskRe: /[A-Za-z &]/,  
								   //stripCharsRe: /[^A-Za-z &]/,
								   maskRe: /[A-Za-z\d &]/,
							       stripCharsRe: /[^A-Za-z\d &]/,
								   margin:'10 0 0 10',
								   width : 500,
								   maxLength : 18,
								   colspan:3,
								   //hidden:true,
								   enforceMaxLength : true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
									//allowBlank:false,
									//readOnly:role=='ANONYMOUS'?false:true,
									/*regex: /[A-Za-z]/,
									regexText: " ",
									validator: function(v) {
									   debugger;
									   if(/[A-Za-z &]/.test(v)){
										   return true;
									   }else return false;
									   //return /[A-Za-z &]/.test(v)?true:"Solo permitido espacios y &";
									}*/
							},{
								xtype : 'displayfield',
								value : SuppAppMsg.supplierForm9,
								height:20,
								margin: '50 0 0 10',
								colspan:3,
								fieldStyle: 'font-weight:bold'
						   },{
								fieldLabel : SuppAppMsg.supplierForm10,
								name : 'calleNumero',
								width:550,
								colspan:3,
								maxLength : 40,
								enforceMaxLength : true,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								xtype:'container',
								layout: {
								    type: 'hbox',
								    pack: 'start',
								    //align: 'stretch'
								},
								items: [
									{
										xtype:'textfield',
										fieldLabel : SuppAppMsg.supplierForm12,
										name : 'codigoPostal',
										id : 'codigoPostal',
										//labelAlign:'left',
										maxLength : 12,
										margin:'10 20 0 0', 
										width:200,
										//colspan:2,
										allowBlank:false,
										   listeners:{
												change: function(field, newValue, oldValue){
													field.setValue(newValue.toUpperCase());
												}
											},
										//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
									    },
				            			{
			            				  xtype: 'button',	
			            				  id : 'searchCP',
			            				  margin:'10 20 0 0', 
			            				  text: SuppAppMsg.supplierForm13,	
			            				  listeners: {		
			            				      click: function() {
			            				    	  var box = Ext.MessageBox.wait( SuppAppMsg.supplierProcessRequest , SuppAppMsg.approvalExecution);
			            				    	  Ext.Ajax.cors = true;
				            						Ext.Ajax.useDefaultXhrHeader = false;
				            						var value = Ext.getCmp('codigoPostal').getValue();
				            						if(value){
				            							if(value != ''){
				            								var token = '1d9ba79b-14b0-4882-b4d1-cb27142e73fe';
				            								var url = 'https://smartechcloud-apps.com/coreapi/api/query_cp/' + value;
						            						Ext.Ajax.request({
						            							url: url,
						            							method: 'GET',
						            							cors: true,
						            							headers: {
						            							    'Access-Control-Allow-Origin': 'https://smartechcloud-apps.com/'
						            							  },
						            							success: function(response){
							            							var text = response.responseText;
							            							var jsonData = Ext.JSON.decode(response.responseText);
							            							colStore.removeAll();
							            							if(jsonData){
							            								if(jsonData.length > 0){
							            									for(var i = 0;i<jsonData.length;i++){
							            										resp = jsonData[i];
							            										var asendamiento = resp.asentamiento
							            										var coloniaUpper = 	asendamiento.toUpperCase();
							            										colStore.add({
										            						        id_: coloniaUpper,
										            						        name: coloniaUpper
										            						    });
							            										//Ext.getCmp('fldEstado').setValue(resp.estado);
							            										Ext.getCmp('fldMunicipio').setValue(resp.municipio);
							            									}
							            								}
							            							}
							            							box.hide();
							            							
						            							},
						            							failure: function (msg) {
						            								alert("Error" + msg);
						            								box.hide();
						            							}
						            						});
				            							}
				            						}else{
			            								Ext.Msg.show({
			            			   					     title:SuppAppMsg.supplierForm14,
			            			   					     msg: SuppAppMsg.supplierForm15,
			            			   					     buttons: Ext.Msg.YES,
			            			   					     icon: Ext.Msg.WARNING
			            								});
			            							}
			            				      }		
			            				  }
				            			}
								]
							},{
									fieldLabel :SuppAppMsg.supplierForm11,
									name : 'delegacionMnicipio',
									id:'fldMunicipio',
									width:300,
									maxLength : 40,
									colspan:3,
									enforceMaxLength : true,
									allowBlank:false,
									   listeners:{
											change: function(field, newValue, oldValue){
												field.setValue(newValue.toUpperCase());
											}
										},
									//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel :SuppAppMsg.supplierForm16,
								name : 'coloniaEXT', 
								id:'coloniaEXT',
								width:300,
								maxLength : 40,
								enforceMaxLength : true,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
											Ext.getCmp('fldColonia').setValue(newValue.toUpperCase());
										}
									},
							},{
								fieldLabel :SuppAppMsg.supplierForm16,
								xtype: 'combobox',
								store:colStore,
								queryMode: 'local',
								displayField: 'name',
								valueField: 'id_',
								triggerAction: 'all',
					            forceSelection: false,
					            editable: true,
					            typeAhead: true,
								name : 'colonia',
								id:'fldColonia',
								editable: false,
								width:400,
								allowBlank:false,
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							    },{
									fieldLabel : SuppAppMsg.supplierForm17,
									name : 'estado',
									id : 'estado',
									xtype: 'combobox',
									typeAhead: true,
									editable: false,
					                typeAheadDelay: 100,
					                allowBlank:false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('STATE', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 250,
									colspan:3
							},{
								fieldLabel :'Ciudad',
								name : 'city', 
								id:'city',
								width:300,
								maxLength : 40,
								enforceMaxLength : true,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
							},{
								fieldLabel : SuppAppMsg.supplierForm18,
								name : 'telefonoDF',
								id:'phoneDF',
								margin:'10 0 0 10',
								width:250,
								maxLength : 20,
								colspan:3,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : 'Fax',
								name : 'faxDF',
								id:'faxDF',
								margin:'10 0 0 10',
								width:250,
								maxLength : 20,
								colspan:3,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								xtype : 'displayfield',
								value : SuppAppMsg.supplierForm19,
								height:20,
								margin: '50 0 0 10',
								colspan:3,
								fieldStyle: 'font-weight:bold'
						   }/*,{
								fieldLabel : SuppAppMsg.supplierForm20,
								name : 'emailComprador',
								width:550,
								colspan:3,
								//tip: SuppAppMsg.supplierForm50,
								regex: /\w@\w/,
							    regexText: SuppAppMsg.supplierForm50,
							    validator: function(v) {
							        return /\w@\w/.test(v)?true:"";
							    },
								allowBlank:false,
					            listeners:{render: function(c) {
							        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
						   }*/,{
								fieldLabel : SuppAppMsg.supplierForm21,
								name : 'nombreContactoCxC',
								id : 'nombreContactoCxC',
								xtype: 'combobox',
								typeAhead: true,
				                typeAheadDelay: 100,
				                allowBlank:false,
				                minChars: 1,
				                queryMode: 'local',
				                readOnly : true,
				                //readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
				                //forceSelection: true,
								store : getAutoLoadUDCStore('USERPURCHASE', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey', 
				                width : 500,
				                editable: false,
								colspan:3,
								listeners: {
							    	select: function (comboBox, records, eOpts) {
							    		var userPurchase = records[0].data;
							    		//Ext.getCmp('emailComprador').setValue(userPurchase.strValue2);
							    		//Ext.getCmp('telefonoCxC').setValue(userPurchase.keyRef);
							    	}
							    }
							},{
								fieldLabel : 'Solicitante de alta',
								emptyText: SuppAppMsg.suppliersName,
								name : 'dischargeApplicant',
								id:'dischargeApplicant',
								hidden:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? true:false,
								margin:'10 0 0 10',
								width:450,
								colspan:3,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							}/*,{
								fieldLabel : SuppAppMsg.supplierForm20,
								name : 'emailComprador',
								id : 'emailComprador',
								width:550,
								colspan:3,
								readOnly:true,
								//tip: SuppAppMsg.supplierForm50,
								regex: /\w@\w/,
							    regexText: SuppAppMsg.supplierForm50,
							    validator: function(v) {
							        return /\w@\w/.test(v)?true:"";
							    },
								allowBlank:false,
					            listeners:{render: function(c) {
							        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
						   },{
								fieldLabel : SuppAppMsg.supplierForm18,
								name : 'telefonoContactoCxC',
								id:'telefonoCxC',
								margin:'10 0 0 10',
								readOnly:true,
								width:300,
								//colspan:1,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.supplierForm24,
								name : 'cargoCxC',
								id:'cargoCxC',
								margin:'10 0 0 10',
								width:300,
								colspan:3,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							}*/,{
								xtype : 'displayfield',
								value : SuppAppMsg.supplierForm25,
								height:20,
								margin: '50 0 0 10',
								colspan:3,
								fieldStyle: 'font-weight:bold'
							},{
								fieldLabel : 'Gerencia General*',
								emptyText: SuppAppMsg.suppliersName, 
								name : 'nombreCxP01',
								id:'nombreCxP01',
								margin:'10 0 0 10',
								width:450,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail + '*',
								name : 'emailCxP01',
								id:'emailCxP01',
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								margin:'10 0 0 10',
								width:300,
								colspan:2,
								allowBlank:false,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							}/*,{
								fieldLabel : SuppAppMsg.supplierForm18,
								name : 'telefonoCxP01',
								id:'telefonoCxP01',
								margin:'10 0 0 10',
								width:300,
								maskRe: /[0-9]/,
								stripCharsRe: /[^0-9]/,
								colspan:2,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							}*/,{
								fieldLabel : 'Gerente de Ventas*',
								emptyText: SuppAppMsg.suppliersName,
								name : 'nombreCxP02',
								id:'nombreCxP02',
								margin:'10 0 0 10',
								width:450,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail + '*',
								name : 'emailCxP02',
								id:'emailCxP02',
								margin:'10 0 0 10',
								//tip: SuppAppMsg.supplierForm50,
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								width:300,
								allowBlank:false,
								colspan:2,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							}/*,{
								fieldLabel : SuppAppMsg.supplierForm18,
								name : 'telefonoCxP02',
								id:'telefonoCxP02',
								margin:'10 0 0 10',
								width:300,
								maskRe: /[0-9]/,
								stripCharsRe: /[^0-9]/,
								colspan:2,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							}*/,{
								fieldLabel : 'Gerente de Producción*',
								emptyText: SuppAppMsg.suppliersName,
								name : 'nombreCxP03',
								id:'nombreCxP03',
								margin:'10 0 0 10',
								width:450,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail ,
								name : 'emailCxP03',
								id:'emailCxP03',
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								margin:'10 0 0 10',
								width:300,
								colspan:2,
								allowBlank:true,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							}/*,{
								fieldLabel : SuppAppMsg.supplierForm48,
								name : 'telefonoCxP03',
								id:'telefonoCxP03',
								margin:'10 0 0 10',
								maskRe: /[0-9]/,
								stripCharsRe: /[^0-9]/,
								width:300,
								colspan:2,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							}*/,{
								fieldLabel : 'Gerente de Cuenta*',
								emptyText: SuppAppMsg.suppliersName,
								name : 'nombreCxP04',
								id:'nombreCxP04',
								margin:'10 0 0 10',
								width:450,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail ,
								name : 'emailCxP04',
								id:'emailCxP04',
								margin:'10 0 0 10',
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								width:300,
								colspan:2,
								allowBlank:true,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							},{
								fieldLabel : 'Gerente Financiero*',
								emptyText: SuppAppMsg.suppliersName,
								name : 'nombreCxP05',
								id:'nombreCxP05',
								margin:'10 0 0 10',
								width:450,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail ,
								name : 'emailCxP05',
								id:'emailCxP05',
								margin:'10 0 0 10',
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								width:300,
								colspan:2,
								allowBlank:true,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							},{
								fieldLabel : 'Encargado de Pedidos*',
								emptyText: SuppAppMsg.suppliersName,
								name : 'nombreCxP06',
								id:'nombreCxP06',
								margin:'10 0 0 10',
								width:450,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail ,
								name : 'emailCxP06',
								id:'emailCxP06',
								margin:'10 0 0 10',
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								width:300,
								colspan:2,
								allowBlank:true,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							},{
								fieldLabel : 'Encargado de Contabilidad*',
								emptyText: SuppAppMsg.suppliersName,
								name : 'nombreCxP07',
								id:'nombreCxP07',
								margin:'10 0 0 10',
								width:450,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.usersEmail ,
								name : 'emailCxP07',
								id:'emailCxP07',
								margin:'10 0 0 10',
								vtype: 'email',
								vtypeText : SuppAppMsg.supplierForm56,
								width:300,
								colspan:2,
								allowBlank:true,
								listeners:{
					            	render: function(c) {
								        Ext.create('Ext.tip.ToolTip', {
								          target: c.getEl(),
								          html: c.tip 
								        });
								      },
									change: function(field, newValue, oldValue){
										field.setValue(newValue.toLowerCase());
									}
								}
							}/*,{
								fieldLabel : SuppAppMsg.supplierForm48,
								name : 'telefonoCxP04',
								id:'telefonoCxP04',
								margin:'10 0 0 10',
								width:300,
								maskRe: /[0-9]/,
								stripCharsRe: /[^0-9]/,
								colspan:2,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							}*/,{
								xtype:'container', 
				                colspan:3,
				                id:'dataTax',
				                //hidden:role=='ROLE_ADMIN' ?false:true,
				                //hidden:true,
				                //hidden:role=='ROLE_ADMIN' || role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' || role=='ROLE_CXP' ||role=='ROLE_CXP_IMPORT' || role=='ROLE_MANAGER'?false:true,
				                defaults : {
									labelWidth : 150,
									xtype : 'textfield',
									margin: '15 0 0 0'
								},
								items: [,								
									/*,{																								
					                    xtype: 'checkbox',
					                    value:false,
					                    name : 'supplierWithOC',
					                    id:'supplierWithOC',
					                    boxLabel: 'Facturas con OC',
					                    margin:'10 0 0 10',
					                    colspan:3,
					                    editable: false,
					                    //hidden: true
									},{
						                xtype: 'checkbox',
						                value:false,
						                name : 'supplierWithoutOC',
						                id:'supplierWithoutOC', 
						                boxLabel: 'Facturas sin OC',
						                margin:'10 0 0 10',
						                colspan:3,
						                editable: false,
						                //hidden: true
								    },*/
								{
									xtype : 'displayfield',
									value : SuppAppMsg.supplierForm27,
									height:20,
									margin: '50 0 0 0',
									colspan:3,
									fieldStyle: 'font-weight:bold'
								},{
									fieldLabel : 'G/L Class', 
									name : 'glClass',
									id:'glClass',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('GLCLASS', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3,
									listeners: {
								    	select: function (comboBox, records, eOpts) {
								    		
								    		var code = records[0].data.udcKey;
								    		if(code == 'PNAC'){
								    			Ext.getCmp('taxArea').setValue('IV16');
								    			Ext.getCmp('catCode29').setValue('NAL');
								    			Ext.getCmp('taxExpl').setValue('V');
								    		}else if(code == 'PEXT'){
								    			Ext.getCmp('taxArea').setValue('MXEXE');
								    			Ext.getCmp('catCode29').setValue('EXT');
								    			Ext.getCmp('taxExpl').setValue('E');
								    		}
								    	}
								    }
								},{
									fieldLabel : SuppAppMsg.purchaseOrderCurrency,
									name : 'currencyCode',
									id : 'currencyCode',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100, 
					                editable: false,
					                allowBlank:true,
					                //blankText: SuppAppMsg.supplierForm74,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('CURRENCY', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : window.navigator.language.startsWith("es", 0)? 'Terminos de Pago':'Payment terms',
									name : 'pmtTrmCxC',
									id : 'pmtTrmCxC',
									xtype: 'combobox',
									typeAhead: true,
									//value:"N60",
					                typeAheadDelay: 100,
					                editable: false,
					                allowBlank:true,
					                minChars: 1,
					                //readOnly:true,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('PMTTERMS', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : 'Tax Area',
									name : 'taxAreaCxC',
									id:'taxArea',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('TAXAREA', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								}/*,{
									fieldLabel : 'Mult Pmts',
									name : 'multPmts',
									id : 'multPmts',
									xtype: 'combobox',
									typeAhead: true,
									//value:"N60",
					                typeAheadDelay: 100,
					                editable: false,
					                allowBlank:true,
					                minChars: 1,
					                //readOnly:true,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('MULTPMTS', '', '', ''),
					                displayField: 'udcKey',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								}*/,{
									fieldLabel : 'Pay Inst',
									name : 'payInstCxC',
									id : 'payInst',
									xtype: 'combobox',
									typeAhead: true,
									value:"T",
					                typeAheadDelay: 100,
					                editable: false,
					                allowBlank:true,
					                minChars: 1,
					                //readOnly:true,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('PAYINST', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								}/*,{
									fieldLabel : 'Código de Impuesto',
									name : 'industryClass',
									id:'industryClass',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,	
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('TAXRATE', '', '', ''),
					                displayField: 'udcKey',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : 'Category Code 25',
									name : 'catCode15',
									id : 'catCode15',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                editable: false,
					                allowBlank:true,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('CATEGORYCODE25', '', '', ''),
					                displayField: 'udcKey',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								}*/,{
									fieldLabel : 'Category Code 17',
									name : 'catCode17',
									id : 'catCode17',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('CATEGORYCODE17', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : 'Category Code 21',
									name : 'catCode21',
									id : 'catCode21',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('CATEGORYCODE21', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : 'Category Code 29',
									name : 'catCode29',
									id : 'catCode29',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('CATEGORYCODE29', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : 'Credit Message',
									name : 'creditMessage',
									id : 'creditMessage',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                allowBlank:true,
					                editable: false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('CREDITMESSAGE', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								},{
									fieldLabel : 'Tax Expl 2',
									name : 'taxExpl2CxC',
									id : 'taxExpl',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100, 
					                editable: false,
					                allowBlank:true,
					                //blankText: SuppAppMsg.supplierForm74,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('TAXEXPL', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 400,
									colspan:3
								}]
							},{
								xtype : 'displayfield',
								value : SuppAppMsg.supplierForm29,
								height:20,
								margin: '50 0 0 10',
								id: 'REPRESENTE_LEGAL',
								colspan:3,
								fieldStyle: 'font-weight:bold'
								},{
									fieldLabel : SuppAppMsg.supplierForm30,
									name : 'tipoIdentificacion',
									id : 'tipoIdentificacion',
									xtype: 'combobox',
									typeAhead: true,
					                typeAheadDelay: 100,
					                editable: false,
					                allowBlank:false,
					                minChars: 1,
					                queryMode: 'local',
					                //forceSelection: true,
									store : getAutoLoadUDCStore('IDENTIFICATIONTYPE', '', '', ''),
					                displayField: 'strValue1',
					                valueField: 'udcKey',
					                width : 250,
									colspan:3
							},{
								fieldLabel : SuppAppMsg.supplierForm31,
								name : 'numeroIdentificacion',
								id : 'numeroIdentificacion',
								width:550,
								colspan:3,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel : SuppAppMsg.supplierForm21,
								name : 'nombreRL',
								id:'nombreRL',
								margin:'10 0 0 10',
								width:400,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.supplierForm22,
								name : 'apellidoPaternoRL',
								id:'apellidoPaternoRL',
								margin:'10 0 0 10',
								width:300,
								colspan:2,
								allowBlank:false,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : SuppAppMsg.supplierForm23,
								name : 'apellidoMaternoRL',
								id:'apellidoMaternoRL',
								margin:'10 0 0 10',
								width:300,
								colspan:3,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								xtype : 'displayfield',
								value : SuppAppMsg.supplierForm32,
								height:20,
								margin: '50 0 0 10',
								colspan:3,
								fieldStyle: 'font-weight:bold'
							}/*,{
								fieldLabel : SuppAppMsg.purchaseOrderCurrency,
								//name : 'currencyCode',
								id:'currencyValidation',
								typeAhead: true,
				                typeAheadDelay: 100,
				                minChars: 1, 
				                queryMode: 'local',
				                //forceSelection: true,
								xtype: 'combobox',
								editable: false,
								store : getAutoLoadUDCStore('CURRENCYVALIDATION', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width : 350,
				                allowBlank:true,
				                colspan:3,
								listeners: {
							    	select: function (comboBox, records, eOpts) {
							    		Ext.getCmp('checkingOrSavingAccount').setValue('0');
							    		var contrib = records[0].data.udcKey; 
							    		if(contrib == '1'){
							    			Ext.getCmp('custBankAcct').show();
							    			Ext.getCmp('controlDigit').show();
							    			Ext.getCmp('description').show();
							    			//Ext.getCmp('checkingOrSavingAccount').show();
							    			
							    			Ext.getCmp('bankTransitNumber').hide();
							    			Ext.getCmp('ibanCode').hide();
							    			Ext.getCmp('swiftCode').hide();
							    			Ext.getCmp('rollNumber').hide();
							    			Ext.getCmp('bankAddressNumber').hide();
							    			Ext.getCmp('bankCountryCode').hide();
							    			
							    			Ext.getCmp('controlDigit').setValue('MX');
							    			Ext.getCmp('custBankAcct').setFieldLabel(SuppAppMsg.supplierForm53);
							    		}else if(contrib == '2'){
							    			
							    			Ext.getCmp('controlDigit').show();
							    			Ext.getCmp('description').show();
							    			//Ext.getCmp('checkingOrSavingAccount').show();
							    			Ext.getCmp('bankTransitNumber').show();
							    			Ext.getCmp('ibanCode').show();
							    			Ext.getCmp('swiftCode').show();
							    			
							    			Ext.getCmp('rollNumber').hide();
							    			Ext.getCmp('bankAddressNumber').hide();
							    			Ext.getCmp('bankCountryCode').hide();
							    			Ext.getCmp('custBankAcct').hide();
							    			
							    			Ext.getCmp('controlDigit').setValue('US');
							    			Ext.getCmp('bankTransitNumber').setFieldLabel(SuppAppMsg.supplierForm54);
							    		}else if(contrib == '3'){
							    			
							    			Ext.getCmp('controlDigit').show();
							    			Ext.getCmp('description').show();
							    			//Ext.getCmp('checkingOrSavingAccount').show();
							    			Ext.getCmp('bankTransitNumber').show();
							    			Ext.getCmp('ibanCode').show();
							    			Ext.getCmp('swiftCode').show();
							    			Ext.getCmp('custBankAcct').show();
							    			
							    			Ext.getCmp('rollNumber').hide();
							    			Ext.getCmp('bankAddressNumber').hide();
							    			Ext.getCmp('bankCountryCode').hide();
							    			
							    			Ext.getCmp('controlDigit').setValue('MX');
							    			Ext.getCmp('custBankAcct').setFieldLabel(SuppAppMsg.supplierForm53);
							    			Ext.getCmp('bankTransitNumber').setFieldLabel(SuppAppMsg.supplierForm55);
							    			
							    		}
							    	
							    	}
							    }
				                //hidden:true
							}*/,{
								fieldLabel : SuppAppMsg.supplierForm33,
								name : 'swiftCode', 
								id : 'swiftCode', 
								width:450,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								colspan:3,
								maxLength : 15,
								stripCharsRe: /[^A-Za-z0-9]/,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel : SuppAppMsg.supplierForm34,
								name : 'ibanCode',
								id:'ibanCode',
								margin:'10 0 0 10',
								maxLength : 34,
								width:400,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
							},{
								fieldLabel : 'Bank Transit Number',
								name : 'bankTransitNumber',
								id : 'bankTransitNumber',
								width:450,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								colspan:3,
								maxLength : 20,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel : 'Cust Bank Account Number',
								name : 'custBankAcct',
								id : 'custBankAcct',
								width:450,
								colspan:3,
								maxLength : 20,
								stripCharsRe: /[^0-9]/,
								allowBlank:true,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel : SuppAppMsg.supplierForm49,
								name : 'controlDigit',
								id : 'controlDigit',
								xtype: 'combobox',
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								typeAhead: true,
				                typeAheadDelay: 100,
				                allowBlank:true,
				                editable: false,
				                minChars: 1,
				                queryMode: 'local',
				                //forceSelection: true,
								store : getAutoLoadUDCStore('CONTROLDIGIT', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width : 450,
								colspan:3
							},{
								fieldLabel : SuppAppMsg.supplierForm51,
								name : 'description',
								id : 'description',
								width:450,
								maskRe: /[A-Za-z &]/,
								stripCharsRe: /[^A-Za-z &]/,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								colspan:3,
								maxLength : 30,
								enforceMaxLength : true,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel : 'Checking or Savings Account',
								name : 'checkingOrSavingAccount',
								id : 'checkingOrSavingAccount',
								xtype: 'combobox',
								typeAhead: true,
								//readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								readOnly:true,
								hidden:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? true:false,
				                typeAheadDelay: 100,
				                allowBlank:true,
				                readOnly:true,
				                editable: false,
				                minChars: 1,
				                queryMode: 'local',
				                //forceSelection: true,
								store : getAutoLoadUDCStore('CHECKORSAVEACCOUNT', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width : 400,
								colspan:3
							},{
								fieldLabel : 'Reference/Roll Number',
								name : 'rollNumber',
								id : 'rollNumber',
								width:450,
								maxLength : 18,
								colspan:3,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								fieldLabel : 'Bank Address Number',
								name : 'bankAddressNumber',
								id : 'bankAddressNumber',
								width:450,
								readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
								colspan:3,
								maxLength : 8,
								enforceMaxLength : true,
								maskRe: /[0-9]/,
								allowBlank:true,
								   listeners:{
										change: function(field, newValue, oldValue){
											field.setValue(newValue.toUpperCase());
										}
									},
								//fieldStyle:role=='ANONYMOUS'?'':'border:2px solid green;'
						   },{
								xtype: 'combobox',
				                name: 'bankCountryCode',
				                id: 'bankCountryCode',
				                fieldLabel: 'Bank Country Code',
				                typeAhead: true,
				                typeAheadDelay: 100,
				                readOnly:role == 'ANONYMOUS' ||role == 'ROLE_SUPPLIER' ? false:true,
				                allowBlank:true,
				                editable: false,
				                margin:'10 0 0 10',
				                minChars: 1,
				                queryMode: 'local',
				                //forceSelection: true,
				                store : getAutoLoadUDCStore('COUNTRY', '', '', ''),
				                displayField: 'strValue1',
				                valueField: 'udcKey',
				                width:400,
				                colspan:3
							},{
								xtype : 'checkbox',
								fieldLabel : 'Proporciona Servicios Especializados (antes Outsourcing) ',
								name : 'outSourcing',
								width : 220,
								labelWidth:220,
								colspan:3,
								checked: false,
								//readOnly:role=='ROLE_SUPPLIER'?true:false
								readOnly: true,
								hidden: role == 'ANONYMOUS'  ? true : false
							},{ 
							    	xtype: 'container',
							    	id:'docLoadSection',
							    	colspan:3,
							    	defaults : {
										labelWidth : 550,
										xtype : 'textfield',
										margin: '15 0 0 0'
									},
							    	items:[{
										xtype : 'displayfield',
										value : SuppAppMsg.supplierForm35,
										margin: '30 0 0 0',
										width:900,
										colspan:3,
										id: 'documents',
										fieldStyle: 'font-weight:bold'
										},{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerRfc',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm36,
										name : 'rfcDocument',
										id:'rfcDocument',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?false:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadRfcDoc',
											id : 'loadRfcDoc',
											text : SuppAppMsg.supplierLoad,
											action : 'loadRfcDoc'
										}]
							    },{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerDom',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm37,
										name : 'domDocument',
										id : 'domDocument',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?false:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadDomDoc',
											id : 'loadDomDoc',
											text : SuppAppMsg.supplierLoad,
											action : 'loadDomDoc'
										}]
							    },{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerBank',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm38,
										name : 'edoDocument',
										id : 'edoDocument',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?false:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadEdoDoc',
											id : 'loadEdoDoc',
											text : SuppAppMsg.supplierLoad,
											action : 'loadEdoDoc'
										}]
							    },{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerIden',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm39,
										name : 'identDocument',
										id : 'identDocument',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?true:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadIdentDoc',
											id : 'loadIdentDoc',
											text : SuppAppMsg.supplierLoad,
											action : 'loadIdentDoc'
										}]
							    },{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerActaConstitutiva',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm40,
										name : 'actaConstitutiva',
										id : 'actaConstitutiva',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?false:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadActaConst',
											id : 'loadActaConst',
											text : SuppAppMsg.supplierLoad,
											action : 'loadActaConst'
										}]
							    },{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerRcp',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm41,
										name : 'rpcDocument',
										id : 'rpcDocument',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?true:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadRpcDocument',
											id : 'loadRpcDocument',
											text : SuppAppMsg.supplierLoad,
											action : 'loadRpcDocument'
										}]
							    }/*,{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerLegalExistence',
									colspan:3,
									width:800,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm42,
										name : 'legalExistence',
										id : 'legalExistence',
										width:400,
										readOnly:true,
										margin: '12 10 0 0',
										//allowBlank:role=='ANONYMOUS'?false:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadlegalExistence',
											id : 'loadlegalExistence',
											text : SuppAppMsg.supplierLoad,
											action : 'loadlegalExistence'
										}]
							    }*/,{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerPatent',
									colspan:3,
									width:800,
									allowBlank:true,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : 'Patente',
										name : 'patent',
										id : 'patent',
										width:400,
										allowBlank:true,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?true:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadPatent',
											id : 'loadPatent',
											text : SuppAppMsg.supplierLoad,
											action : 'loadPatent',
											allowBlank:true,
										}]
							    },{
							        xtype: 'container',
							        layout: 'hbox',
							        id: 'documentContainerBusinessValues',
							        colspan: 3,
							        width: 800,
							        allowBlank: true,
							        //hidden: role == 'ANONYMOUS' ? false : true,
							        defaults: {
							            labelWidth: 150,
							            xtype: 'textfield',
							            margin: '12 0 0 0'
							        },
							        items: [{
							            xtype: 'textfield',
							            fieldLabel: 'Código de Valores y Comportamientos de Socios de Negocios',
							            name: 'businessValues',
							            id: 'businessValues',
							            width: 400,
							            allowBlank: role == 'ANONYMOUS' ? true : true,
							            readOnly: true,
							            margin: '12 10 0 0'
							        }, {
							            xtype: 'button',
							            width: 100,
							            itemId: 'loadBusinessValues',
							            id: 'loadBusinessValues',
							            text: SuppAppMsg.supplierLoad,
							            action: 'loadBusinessValues',
							            allowBlank: true
							        }]
							    },{
							        xtype: 'container',
							        layout: 'hbox',
							        id: 'documentContainerAntiBribery',
							        colspan: 3,
							        width: 800,
							        allowBlank: true,
							        defaults: {
							            labelWidth: 150,
							            xtype: 'textfield',
							            margin: '12 0 0 0'
							        },
							        items: [{
							            xtype: 'textfield',
							            fieldLabel: 'Compromiso Antisoborno Socios de Negocios',
							            name: 'antiBribery',
							            id: 'antiBribery',
							            width: 400,
							            allowBlank: role == 'ANONYMOUS' ? true : true,
							            readOnly: true,
							            margin: '12 10 0 0'
							        }, {
							            xtype: 'button',
							            width: 100,
							            itemId: 'loadAntiBribery',
							            id: 'loadAntiBribery',
							            text: SuppAppMsg.supplierLoad,
							            action: 'loadAntiBribery',
							            allowBlank: true
							        }]
							    },{
							        xtype: 'container',
							        layout: 'hbox',
							        id: 'documentContainerSupplierSurvey',
							        colspan: 3,
							        width: 800,
							        allowBlank: true,
							        defaults: {
							            labelWidth: 150,
							            xtype: 'textfield',
							            margin: '12 0 0 0'
							        },
							        items: [{
							            xtype: 'textfield',
							            fieldLabel: 'Cuestionario Socios de Negocios',
							            name: 'supplierSurvey',
							            id: 'supplierSurvey',
							            width: 400,
							            allowBlank: role == 'ANONYMOUS' ? true : true,
							            readOnly: true,
							            margin: '12 10 0 0'
							        }, {
							            xtype: 'button',
							            width: 100,
							            itemId: 'loadSupplierSurvey',
							            id: 'loadSupplierSurvey',
							            text: SuppAppMsg.supplierLoad,
							            action: 'loadSupplierSurvey',
							            allowBlank: true
							        }]
							    },// 4. Solicitud de Inscripción de Proveedor
							    {
							        xtype: 'container',
							        layout: 'hbox',
							        id: 'documentContainerSupplierRequest',
							        colspan: 3,
							        width: 800,
							        allowBlank: true,
							        defaults: {
							            labelWidth: 150,
							            xtype: 'textfield',
							            margin: '12 0 0 0'
							        },
							        items: [{
							            xtype: 'textfield',
							            fieldLabel: 'Solicitud de Inscripción de Proveedor',
							            name: 'supplierRequest',
							            id: 'supplierRequest',
							            width: 400,
							            allowBlank: role == 'ANONYMOUS' ? true : true,
							            readOnly: true,
							            margin: '12 10 0 0'
							        }, {
							            xtype: 'button',
							            width: 100,
							            itemId: 'loadSupplierRequest',
							            id: 'loadSupplierRequest',
							            text: SuppAppMsg.supplierLoad,
							            action: 'loadSupplierRequest',
							            allowBlank: true
							        }]
							    },

							    // 5. Política de Transparencia y Ética
							    {
							        xtype: 'container',
							        layout: 'hbox',
							        id: 'documentContainerEthicsPolicy',
							        colspan: 3,
							        width: 800,
							        allowBlank: true,
							        defaults: {
							            labelWidth: 150,
							            xtype: 'textfield',
							            margin: '12 0 0 0'
							        },
							        items: [{
							            xtype: 'textfield',
							            fieldLabel: 'Política de Transparencia, Ética y Anticorrupción',
							            name: 'ethicsPolicy',
							            id: 'ethicsPolicy',
							            width: 400,
							            allowBlank: role == 'ANONYMOUS' ? true : true,
							            readOnly: true,
							            margin: '12 10 0 0'
							        }, {
							            xtype: 'button',
							            width: 100,
							            itemId: 'loadEthicsPolicy',
							            id: 'loadEthicsPolicy',
							            text: SuppAppMsg.supplierLoad,
							            action: 'loadEthicsPolicy',
							            allowBlank: true
							        }]
							    },

							    // 6. Información del proveedor
							    {
							        xtype: 'container',
							        layout: 'hbox',
							        id: 'documentContainerSupplierInfo',
							        colspan: 3,
							        width: 800,
							        allowBlank: true,
							        defaults: {
							            labelWidth: 150,
							            xtype: 'textfield',
							            margin: '12 0 0 0'
							        },
							        items: [{
							            xtype: 'textfield',
							            fieldLabel: 'Información del proveedor',
							            name: 'supplierInfo',
							            id: 'supplierInfo',
							            width: 400,
							            allowBlank: role == 'ANONYMOUS' ? true : true,
							            readOnly: true,
							            margin: '12 10 0 0'
							        }, {
							            xtype: 'button',
							            width: 100,
							            itemId: 'loadSupplierInfo',
							            id: 'loadSupplierInfo',
							            text: SuppAppMsg.supplierLoad,
							            action: 'loadSupplierInfo',
							            allowBlank: true
							        }]
							    },{
									xtype: 'container',
									layout: 'hbox',
									id:'documentContainerForeingResidence',
									colspan:3,
									width:800,
									allowBlank:true,
									//hidden:role=='ANONYMOUS'?false:true,
									defaults : {
										labelWidth : 150,
										xtype : 'textfield',
										margin: '12 0 0 0'
									},
							        items:[{
							        	xtype : 'textfield',
										fieldLabel : SuppAppMsg.supplierForm43,
										name : 'foreingResidence',
										id : 'foreingResidence',
										width:400,
										allowBlank:true,
										readOnly:true,
										margin: '12 10 0 0',
										allowBlank:role=='ANONYMOUS'?true:true
									    },{
											xtype: 'button',
											width:100,
											itemId : 'loadForeingResidence',
											id : 'loadForeingResidence',
											text : SuppAppMsg.supplierLoad,
											action : 'loadForeingResidence',
											allowBlank:true,
										}]
							    },{
							    	//OUTSOURCING
									xtype: 'container',
									layout: 'vbox',
									id:'documentContainerOutSourcing',
									colspan:3,
									width:800,
									hidden:true,
							        items:[
							        	{
							        		xtype:'container',
							        		layout:'hbox',
							        		defaults : {
												labelWidth : 150,
												xtype : 'textfield',
												margin: '0 0 0 0'
											},
							        		items:[
							        			{
										        	xtype : 'textfield',
													fieldLabel : SuppAppMsg.outsourcingSTPSLabel,
													name : 'textSTPS',
													id : 'textSTPS',
													width:400,
													readOnly:true,
													allowBlank:true
												    },{
														xtype: 'button',
														width:100,
														itemId : 'loadSTPS',
														margin:'0 0 0 12',
														id : 'loadSTPS',
														text : SuppAppMsg.supplierLoad,
														action : 'loadSTPS'
													}
							        		]
							        	
							        },{
						        		xtype:'container',
						        		layout:'hbox',
						        		defaults : {
											labelWidth : 150,
											xtype : 'textfield',
											margin: '15 10 0 0'
										},
						        		items:[
						        			{
									        	xtype : 'textfield',
												fieldLabel : SuppAppMsg.outsourcingIMSSLabel,
												name : 'textIMSS',
												id : 'textIMSS',
												width:400,
												readOnly:true,
												allowBlank:true
											    },{
													xtype: 'button',
													width:100,
													itemId : 'loadIMSS',
													id : 'loadIMSS',
													text : SuppAppMsg.supplierLoad,
													action : 'loadIMSS'
											}
						        			]
							        }]
							    }]
							    },{
									xtype : 'displayfield',
									value : SuppAppMsg.supplierForm44,
									margin: '30 0 0 0',
									colspan:3,
									id : 'camposobligatorios',
									fieldStyle: 'font-weight:bold'
								},{
									xtype : 'displayfield',
									value : '',
									id:'hrefFileList',
									height:20,
									width:800,
									margin: '10 0 20 10',
									colspan:3

								},{
								xtype : 'displayfield',
								value : 'DOCUMENTACIÓN DE USO INTERNO:',
								height:20,
								margin: '50 20 0 10',
								hidden:true,
								colspan:3,
								fieldStyle: 'font-weight:bold'
							},{
								xtype : 'displayfield',
								value : '',
								id:'internalFileList',
								hidden:role=='ANONYMOUS'?true:false,
								height:20,
								width:800,
								margin: '10 0 0 10',
								colspan:3

							},{
									xtype: 'button',
									iconCls : 'icon-save',
									width:250,
									itemId : 'sendSupplierForm',
									id : 'sendSupplierForm',
									text : SuppAppMsg.supplierForm45,
									action : 'sendSupplierForm',
									colspan:3,
									fieldStyle: 'padding-left:350px;',
									hidden:role=='ANONYMOUS'?false:true
								},{
									xtype: 'button',
									iconCls : 'icon-save',
									width:250,
									itemId : 'updateSupplierForm',
									id : 'updateSupplierForm',
									text : SuppAppMsg.supplierForm46,
									action : 'updateSupplierForm',
									colspan:3,
									hidden:	role=='ROLE_ADMIN'
											||role=='ROLE_CXP'
											||role=='ROLE_CXP_IMPORT'
											||role == 'ROLE_SUPPLIER'
											||role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE'
											||role=='ROLE_PURCHASE_IMPORT'?false:true,
									/*hidden : role=='ROLE_ADMIN'
											||role=='ROLE_CXP'
											||role=='ROLE_CXP_IMPORT'
											||role == 'ROLE_PURCHASE'
											||role=='ROLE_PURCHASE_IMPORT'?false:true,*/
									fieldStyle: 'padding-left:350px;'
								},{
									xtype: 'button',
									iconCls : 'icon-save',
									width:250,
									itemId : 'updateTaxData',
									id : 'updateTaxData',
									text : 'Actualizar códigos de categoría',
									action : 'updateTaxData',
									colspan:3,
									fieldStyle: 'padding-left:350px;',
									hidden:role=='ROLE_TAX'?false:true
								},{
									xtype: 'button',
									iconCls : 'icon-save',
									width:250,
									itemId : 'updateEmailSupplierForm',
									id : 'updateEmailSupplierForm',
									text : 'Actualizar Email',
									action : 'updateEmailSupplierForm',
									colspan:3,
									/*hidden:	role=='ROLE_ADMIN'
											||role=='ROLE_CXP'
											||role=='ROLE_CXP_IMPORT'
											||role == 'ROLE_SUPPLIER'
											||role == 'ROLE_PURCHASE'
											||role=='ROLE_PURCHASE_IMPORT'?false:true,*/
									hidden : role=='ROLE_ADMIN'?false:true,
									fieldStyle: 'padding-left:350px;'
								},{
									xtype: 'button',
									iconCls : 'icon-save',
									width:250,
									itemId : 'updateSupplierFormDraft',
									id : 'updateSupplierFormDraft',
									text : SuppAppMsg.supplierForm47,
									action : 'updateSupplierFormDraft',
									colspan:3,
									hidden:role=='ANONYMOUS'?false:true,
									fieldStyle: 'padding-left:350px;'
								}
								,{
									xtype : 'displayfield',
									value : '',
									height:20,
									margin: '20 0 0 10',
									colspan:3

								},{
									xtype : 'hidden',
									name : 'fileList'
								},,{
									name : 'regionesTmp',
									hidden:true
							    },{
									name : 'categoriasTmp',
									hidden:true,
									colspan:3
								 },{
									name : 'currentApprover',
									hidden:true
								 },{
									name : 'nextApprover',
									hidden:true
								 },{
									name : 'tipoMovimiento',
									hidden:true
								 },{
									name : 'name',
									hidden:true
								 }
								 


					]}
			];

		


		
		this.callParent(arguments);
	},
    changeLabel : function(btn) {
        var frm = btn.up('form'),
        lang = btn.getText();
        var tfs = frm.query('.textfield');
        Ext.each(tfs, function(tf){
        	if (typeof tf.translate !== "undefined") {
                tf.setFieldLabel(tf.translate[lang]);
        	}
        });
       
        var tfs = frm.query('.displayfield');
        Ext.each(tfs, function(tf){
        	if (typeof tf.translate !== "undefined") {
                tf.setValue(tf.translate[lang]);
        	}
        });
        
        var tfs = frm.query('.combobox');
        Ext.each(tfs, function(tf){
        	if (typeof tf.translate !== "undefined") {
                tf.setFieldLabel(tf.translate[lang]);
        	}
        });
        
        var tfs = frm.query('.button');
        Ext.each(tfs, function(tf){
        	if (typeof tf.translate !== "undefined") {
                tf.setText(tf.translate[lang]);
        	}
        });
      }

});