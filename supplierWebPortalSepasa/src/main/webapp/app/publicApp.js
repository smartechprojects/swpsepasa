var SuppAppMsg = {};

Ext.application({
    name: 'SupplierApp',
    controllers: ['Supplier'],
    autoCreateViewport: false,
	appFolder: 'app',
	launch: function () {
        Ext.QuickTips.init();
        app = this;

		var langu = window.navigator.language;
		var lang = "";
		if(langu.startsWith("es", 0)){
			lang = "es";
		}else{
			lang = "en";
		}
		
		var me = this;
		
		var box = Ext.MessageBox.wait("Loading data...","");
		
		Ext.Ajax.request({
		    url: 'getLocalization.action',
		    method: 'GET',
		    params: {
		    	lang : lang
	        },
		    success: function(fp, o) {
		    	var resp = Ext.decode(fp.responseText, true);
		    	SuppAppMsg = resp.data;
		    	
		    	ticketAccepted = document.getElementById("ticketAccepted").value;
		    	if(ticketAccepted != null && ticketAccepted != ''){
			    	Ext.Ajax.request({
					    url: 'public/getByTicketId.action',
					    method: 'GET',
					    params: {
					    	ticketId : ticketAccepted
				        },
					    success: function(fp, o) {
					    	var resp = Ext.decode(fp.responseText, true);
							if(!me.mainPanel){
								me.mainPanel = Ext.create('Ext.panel.Panel', {
							        autoWidth: true,
							        renderTo: 'content',
							        layout:'fit',
							        border:false,
							        frame:false,
							        items: [{
							        	xtype: 'supplierForm'
									}]
								});
								var rec = Ext.create('SupplierApp.model.Supplier');
								rec.set(resp.data);
								var form = Ext.getCmp('supplierFormId').getForm();
								form.loadRecord(rec);
								
								if(rec.data.country != 'MX'){
					    			//Ext.getCmp('supRfc').setReadOnly(true);
					    			Ext.getCmp('taxId').allowBlank=false;
				        			Ext.getCmp('taxId').show();
				        			Ext.getCmp('supRfc').allowBlank=true;
				        			Ext.getCmp('supRfc').hide();
				        			//Ext.getCmp('regimeType').allowBlank = true;
				        			//Ext.getCmp('fisicaMoralId').setReadOnly(false);
				        		}else{
					    			//Ext.getCmp('supRfc').setReadOnly(false);
					    			Ext.getCmp('supRfc').allowBlank=false;
				        			Ext.getCmp('supRfc').show();
				        			Ext.getCmp('taxId').allowBlank=true;
				        			Ext.getCmp('taxId').hide();
				        			//Ext.getCmp('regimeType').allowBlank = false;
				        		}
								
								if(rec.data.fisicaMoral == 'F'){
									Ext.getCmp('curp').show();
					    			Ext.getCmp('curp').allowBlank=false;
					    			
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
								}else{
									Ext.getCmp('curp').hide();
					    			Ext.getCmp('curp').allowBlank=true;
					    			
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
								}
								
								if(rec.data.country == 'MX'){
				        			
				        			Ext.getCmp('fldColonia').show();
					    			Ext.getCmp('fldColonia').allowBlank=false;
					    			Ext.getCmp('coloniaEXT').hide();
					    			Ext.getCmp('coloniaEXT').allowBlank=true;
					    			
					    			Ext.getCmp('fldMunicipio').show();
									Ext.getCmp('fldMunicipio').allowBlank=false;
				        		}else{
				        			
				        			Ext.getCmp('coloniaEXT').allowBlank=false;
					    			Ext.getCmp('coloniaEXT').show();
					    			Ext.getCmp('coloniaEXT').setValue(response.data.colonia);
					    			
					    			Ext.getCmp('fldColonia').hide();
					    			Ext.getCmp('fldColonia').allowBlank=true;
					    			
					    			Ext.getCmp('fldMunicipio').hide();
									Ext.getCmp('fldMunicipio').allowBlank=true;
				        		}
								
								var fileList = rec.data.fileList;
	    			        	var hrefList ="";
	    			        	var r2 = [];
	    			        	var href ="";
	    			        	var fileHref="";
	    			        	
	    			        	var r1 = fileList.split("_FILE:");
	    			        	//for (var index = 0; index < r1.length; index++) {
	    			        	var inx = r1.length;
	    			        	for (var index = inx - 1; index >= 0; index--) {
	    			        		r2 = r1[index].split("_:_");
	    				        		if(r2[0]  != ""){
	    					        	href = "documents/openDocumentSupplier.action?id=" + r2[0];
	    								fileHref = "*** <a href= '" + href + "' target='_blank'>" +  r2[1] + "</a> ||" + r2[2];
	    								hrefList = "<p>"  + hrefList + fileHref + "</p>";
	    			        		}
	    			        	} 
	    			        	Ext.getCmp('hrefFileList').setValue(hrefList);
								
								Ext.getCmp('docLoadSection').query('textfield').forEach(function(field) {
								    field.allowBlank = true;

								    // Esto fuerza la validación si ya estaba hecha (opcional)
								    if (field.isValid()) {
								        field.clearInvalid();
								    } else {
								        field.validate();
								    }
								});
								
								Ext.getCmp('dataTax').hide();
								
								/*if(rec.data.country == 'MX'){
				    				Ext.getCmp('rfcDocument').allowBlank = false;
				    	    		Ext.getCmp('actaConstitutiva').allowBlank = false;
				    	    		Ext.getCmp('domDocument').allowBlank = false;
				    	    		Ext.getCmp('edoDocument').allowBlank = false;
				    	    		Ext.getCmp('identDocument').allowBlank = false;
				    	    		Ext.getCmp('regimeType').allowBlank = false;
				    	    		
				    	    		Ext.getCmp('foreingResidence').allowBlank = true;
				    	    		Ext.getCmp('legalExistence').allowBlank = true;
				    	    		
				    			}else{
				    				Ext.getCmp('rfcDocument').allowBlank = true;
				    	    		Ext.getCmp('actaConstitutiva').allowBlank = true;
				    	    		Ext.getCmp('domDocument').allowBlank = true;
				    	    		Ext.getCmp('edoDocument').allowBlank = true;
				    	    		Ext.getCmp('identDocument').allowBlank = true;
				    	    		Ext.getCmp('regimeType').allowBlank = true;
				    	    		
				    	    		Ext.getCmp('foreingResidence').allowBlank = false;
				    	    		Ext.getCmp('legalExistence').allowBlank = false;
				    			}
								
								if(rec.data.fisicaMoral != '1'){
					    			
					    			var taxIdCombo = Ext.getCmp('taxRateSupId');
						    		var store = taxIdCombo.getStore();
						    		store.removeAll();
						    		store.proxy.extraParams = { 
						    				query:'',
						    				udcSystem : 'TAXRATE',
						    				udcKey : '',
						    				systemRef : '',
						    				keyRef : rec.data.fisicaMoral
						        	    	}
						        	
						        	store.load();
				        		}else{
				        			var taxIdCombo = Ext.getCmp('taxRateSupId');
						    		var store = taxIdCombo.getStore();
						    		store.removeAll();
						    		store.proxy.extraParams = { 
						    				query:'',
						    				udcSystem : 'TAXRATE',
						    				udcKey : '',
						    				systemRef : '',
						    				keyRef : rec.data.fisicaMoral
						        	    	}
						        	
						        	store.load();
				        		}
								
								var foreignAccount = rec.data.foreignAccount;
								
								switch (foreignAccount) {

				    		 	case 'PTC':			
				    		 		Ext.getCmp('bankNameForeignId').allowBlank = false;//Banco mexicano
                                	Ext.getCmp('custBankAcctId').allowBlank = false;//Número de Cuenta
                                	Ext.getCmp('foreignCuentaClabeId').allowBlank = true; //Cuenta Clabe
                                	Ext.getCmp('swiftCodeId').allowBlank = true;
                                	Ext.getCmp('ibanCodeId').allowBlank = true;
                                	Ext.getCmp('bankTransitNumberId').allowBlank = true;//Banco Extranjero
                                	Ext.getCmp('controlDigitForeignId').allowBlank = true;//CIE
                                	Ext.getCmp('rollNumberForeignId').allowBlank = true; //Num Ref
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(false);
                                	Ext.getCmp('custBankAcctId').setReadOnly(false);
                                	Ext.getCmp('foreignCuentaClabeId').setReadOnly(false);
                                	Ext.getCmp('swiftCodeId').setReadOnly(false);
                                	Ext.getCmp('ibanCodeId').setReadOnly(true);
                                	Ext.getCmp('bankTransitNumberId').setReadOnly(true);
                                	Ext.getCmp('controlDigitForeignId').setReadOnly(true);
                                	Ext.getCmp('rollNumberForeignId').setReadOnly(true);
                                	
                                	Ext.getCmp('custBankAcctId').maxLength=10;
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(true);
                                    break;
                                case 'CIL_RF':		
                                	Ext.getCmp('bankNameForeignId').allowBlank = false;//Banco mexicano
                                	Ext.getCmp('controlDigitForeignId').allowBlank = false;//CIE
                                	Ext.getCmp('rollNumberForeignId').allowBlank = false; //Num Ref
                                	
                                	Ext.getCmp('custBankAcctId').allowBlank = true;//Número de Cuenta
                                	Ext.getCmp('foreignCuentaClabeId').allowBlank = true; //Cuenta Clabe
                                	Ext.getCmp('swiftCodeId').allowBlank = true;
                                	Ext.getCmp('ibanCodeId').allowBlank = true;
                                	Ext.getCmp('bankTransitNumberId').allowBlank = true;//Banco Extranjero
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(false);
                                	Ext.getCmp('controlDigitForeignId').setReadOnly(false);
                                	Ext.getCmp('rollNumberForeignId').setReadOnly(false);
                                	
                                	Ext.getCmp('custBankAcctId').setReadOnly(true);
                                	Ext.getCmp('foreignCuentaClabeId').setReadOnly(true);
                                	Ext.getCmp('swiftCodeId').setReadOnly(true);
                                	Ext.getCmp('ibanCodeId').setReadOnly(true);
                                	Ext.getCmp('bankTransitNumberId').setReadOnly(true);
                                	
                                	Ext.getCmp('custBankAcctId').maxLength=20;
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(true);
                                    break;
                                case 'CIL_RV':
                                	Ext.getCmp('bankNameForeignId').allowBlank = false;//Banco mexicano
                                	Ext.getCmp('controlDigitForeignId').allowBlank = false;//CIE
                                	Ext.getCmp('rollNumberForeignId').allowBlank = true; //Num Ref
                                	
                                	Ext.getCmp('custBankAcctId').allowBlank = true;//Número de Cuenta
                                	Ext.getCmp('foreignCuentaClabeId').allowBlank = true; //Cuenta Clabe
                                	Ext.getCmp('swiftCodeId').allowBlank = true;
                                	Ext.getCmp('ibanCodeId').allowBlank = true;
                                	Ext.getCmp('bankTransitNumberId').allowBlank = true;//Banco Extranjero
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(false);
                                	Ext.getCmp('controlDigitForeignId').setReadOnly(false);
                                	Ext.getCmp('rollNumberForeignId').setReadOnly(false);
                                	
                                	Ext.getCmp('custBankAcctId').setReadOnly(true);
                                	Ext.getCmp('foreignCuentaClabeId').setReadOnly(true);
                                	Ext.getCmp('swiftCodeId').setReadOnly(true);
                                	Ext.getCmp('ibanCodeId').setReadOnly(true);
                                	Ext.getCmp('bankTransitNumberId').setReadOnly(true);
                                	
                                	Ext.getCmp('custBankAcctId').maxLength=20;
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(true);
                                    break;
                                case 'PSC_BE':
                                	Ext.getCmp('bankTransitNumberId').allowBlank = false;//Banco Extranjero
                                	Ext.getCmp('custBankAcctId').allowBlank = false;//Número de Cuenta
                                	Ext.getCmp('swiftCodeId').allowBlank = false;
                                	Ext.getCmp('ibanCodeId').allowBlank = true;
                                	
                                	Ext.getCmp('bankNameForeignId').allowBlank = true;
                                	Ext.getCmp('foreignCuentaClabeId').allowBlank = true;
                                	Ext.getCmp('controlDigitForeignId').allowBlank = true;
                                	Ext.getCmp('rollNumberForeignId').allowBlank = true;
                                	
                                	Ext.getCmp('bankTransitNumberId').setReadOnly(false);
                                	Ext.getCmp('custBankAcctId').setReadOnly(false);
                                	Ext.getCmp('swiftCodeId').setReadOnly(false);
                                	Ext.getCmp('ibanCodeId').setReadOnly(false);
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(true);
                                	Ext.getCmp('foreignCuentaClabeId').setReadOnly(true);
                                	Ext.getCmp('controlDigitForeignId').setReadOnly(true);
                                	Ext.getCmp('rollNumberForeignId').setReadOnly(true);
                                	
                                	Ext.getCmp('custBankAcctId').maxLength=20;
                                    break; 
                                case 'PSC_OB':
                                	Ext.getCmp('bankNameForeignId').allowBlank = false;//Banco mexicano
                                	Ext.getCmp('foreignCuentaClabeId').allowBlank = false; //Cuenta Clabe
                                	Ext.getCmp('custBankAcctId').allowBlank = true;//Número de Cuenta
                                	
                                	Ext.getCmp('swiftCodeId').allowBlank = true;
                                	Ext.getCmp('ibanCodeId').allowBlank = true;
                                	Ext.getCmp('bankTransitNumberId').allowBlank = true;//Banco Extranjero
                                	Ext.getCmp('controlDigitForeignId').allowBlank = true;//CIE
                                	Ext.getCmp('rollNumberForeignId').allowBlank = true; //Num Ref
                                	
                                	Ext.getCmp('bankNameForeignId').setReadOnly(false);
                                	Ext.getCmp('custBankAcctId').setReadOnly(false);
                                	Ext.getCmp('foreignCuentaClabeId').setReadOnly(false);
                                	
                                	Ext.getCmp('swiftCodeId').setReadOnly(true);
                                	Ext.getCmp('ibanCodeId').setReadOnly(true);
                                	Ext.getCmp('bankTransitNumberId').setReadOnly(true);
                                	Ext.getCmp('controlDigitForeignId').setReadOnly(true);
                                	Ext.getCmp('rollNumberForeignId').setReadOnly(true);
                                	
                                	Ext.getCmp('custBankAcctId').maxLength=20;
                                    break; 
                                  default:
                                	  Ext.getCmp('bankNameForeignId').allowBlank = true; //Banco mexicano
                                	Ext.getCmp('foreignCuentaClabeId').allowBlank = true; //Cuenta Clabe
                              	Ext.getCmp('custBankAcctId').allowBlank = true; //Número de Cuenta
                              	Ext.getCmp('swiftCodeId').allowBlank = true;
                              	Ext.getCmp('ibanCodeId').allowBlank = true;
                              	Ext.getCmp('bankTransitNumberId').allowBlank = true; //Banco Extranjero
                              	Ext.getCmp('controlDigitForeignId').allowBlank = true; //CIE
                              	Ext.getCmp('rollNumberForeignId').allowBlank = true; //Num Ref
                              	
                              	Ext.getCmp('bankNameForeignId').setReadOnly(true);
                              	Ext.getCmp('custBankAcctId').setReadOnly(true);
                              	Ext.getCmp('foreignCuentaClabeId').setReadOnly(true);
                              	Ext.getCmp('swiftCodeId').setReadOnly(true);
                              	Ext.getCmp('ibanCodeId').setReadOnly(true);
                              	Ext.getCmp('bankTransitNumberId').setReadOnly(true);
                              	Ext.getCmp('controlDigitForeignId').setReadOnly(true);
                              	Ext.getCmp('rollNumberForeignId').setReadOnly(true);
                                    break;
                                    	
                                
								}
								
								var localAccount = rec.data.localAccount;
								
								switch (localAccount) {
                                case 'PTC':			
                                	Ext.getCmp('bankNameId').allowBlank = false;
                                	Ext.getCmp('cuentaBancariaId').allowBlank = false;
                                	Ext.getCmp('cuentaClabeId').allowBlank = true;
                                	Ext.getCmp('controlDigitId').allowBlank = true;
                                	Ext.getCmp('rollNumberId').allowBlank = true;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(false);
                                	Ext.getCmp('cuentaBancariaId').setReadOnly(false);
                                	Ext.getCmp('cuentaClabeId').setReadOnly(false);
                                	Ext.getCmp('controlDigitId').setReadOnly(true);
                                	Ext.getCmp('rollNumberId').setReadOnly(true);
                                	
                                	Ext.getCmp('cuentaBancariaId').maxLength=10;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(true);
                                    break;
                                case 'PSC':
                                	Ext.getCmp('bankNameId').allowBlank = false;
                                	Ext.getCmp('cuentaBancariaId').allowBlank = true;
                                	Ext.getCmp('cuentaClabeId').allowBlank = false;
                                	Ext.getCmp('controlDigitId').allowBlank = true;
                                	Ext.getCmp('rollNumberId').allowBlank = true;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(false);
                                	Ext.getCmp('cuentaBancariaId').setReadOnly(false);
                                	Ext.getCmp('cuentaClabeId').setReadOnly(false);
                                	Ext.getCmp('controlDigitId').setReadOnly(true);
                                	Ext.getCmp('rollNumberId').setReadOnly(true);
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(false);
                                	Ext.getCmp('cuentaBancariaId').maxLength=20;
                                	
                                    break;
                                case 'CIL_RF':
                                	Ext.getCmp('bankNameId').allowBlank = false;
                                	Ext.getCmp('cuentaBancariaId').allowBlank = true;
                                	Ext.getCmp('cuentaClabeId').allowBlank = true;
                                	Ext.getCmp('controlDigitId').allowBlank = false;
                                	Ext.getCmp('rollNumberId').allowBlank = false;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(false);
                                	Ext.getCmp('cuentaBancariaId').setReadOnly(true);
                                	Ext.getCmp('cuentaClabeId').setReadOnly(true);
                                	Ext.getCmp('controlDigitId').setReadOnly(false);
                                	Ext.getCmp('rollNumberId').setReadOnly(false);
                                	
                                	Ext.getCmp('cuentaBancariaId').maxLength=20;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(true);
                                    break; 
                                case 'CIL_RV':
                                	Ext.getCmp('bankNameId').allowBlank = false;
                                	Ext.getCmp('cuentaBancariaId').allowBlank = true;
                                	Ext.getCmp('cuentaClabeId').allowBlank = true;
                                	Ext.getCmp('controlDigitId').allowBlank = false;
                                	Ext.getCmp('rollNumberId').allowBlank = true;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(false);
                                	Ext.getCmp('cuentaBancariaId').setReadOnly(true);
                                	Ext.getCmp('cuentaClabeId').setReadOnly(true);
                                	Ext.getCmp('controlDigitId').setReadOnly(false);
                                	Ext.getCmp('rollNumberId').setReadOnly(false);
                                	
                                	Ext.getCmp('cuentaBancariaId').maxLength=20;
                                	
                                	Ext.getCmp('bankNameId').setReadOnly(true);
                                    break;
                                    
                                  default:
                                	Ext.getCmp('bankNameId').allowBlank = true;
                                	Ext.getCmp('cuentaBancariaId').allowBlank = true;
                                	Ext.getCmp('cuentaClabeId').allowBlank = true;
                                	Ext.getCmp('controlDigitId').allowBlank = true;
                                	Ext.getCmp('rollNumberId').allowBlank = true;
                                    break;
                                    	
                                }
								
								Ext.Ajax.request({
									url : 'documents/listDocumentsAttachId.action',
									method : 'GET',
										params : {
											attachId : rec.get('attachId')
										},
										success : function(response,opts) {
											response = Ext.decode(response.responseText);
											
											var index = 0;
											var files = "";
											for (index = 0; index < response.data.length; index++) {
												var href = "documents/openDocument.action?id=" + response.data[index].id;
												var fileHref = response.data[index].uploadDate + "&nbsp;" +  "&nbsp;<a href= '" + href + "' target='_blank'>" +  response.data[index].name + "</a>";
					                            //files = files + "> " + fileHref + " - " + response.data[index].size + " bytes  "  +  "&nbsp;&nbsp;&nbsp;" + "<A HREF='javascript:deleteDocumentWithAttach(" + response.data[index].id + ",\"" + rec.get('attachId') + "\"," + "\"hrefFileList\"" + ")'>Eliminar</A>" + "<br />";	
					                            files = files + "> " + fileHref + " - " + response.data[index].size + " bytes  "  + "<br />";
					                            
					                            //Validacion documentos
					                            var docType = response.data[index].documentType;
					                            
					                            if(rec.data.country == 'MX'){
					                            	switch (docType) {
						                            
					                                case 'loadRfcDoc':			
					                                	Ext.getCmp('rfcDocument').allowBlank = true;
					                                    break;
					                                case 'loadActaConst':
					                                	Ext.getCmp('actaConstitutiva').allowBlank = true;
					                                    break;
					                                case 'loadDomDoc':
					                                	Ext.getCmp('domDocument').allowBlank = true;
					                                    break; 
					                                case 'loadEdoDoc':
					                                	Ext.getCmp('edoDocument').allowBlank = true;
					                                    break;
					                                case 'loadIdentDoc':
					                                	Ext.getCmp('identDocument').allowBlank = true;
					                                    break;
					                                    
					                                  default:
					                                    break;
					                                
						                            }
					                            }else{
					                            	switch (docType) {
						                            
					                                case 'loadForeingResidence':			
					                                	Ext.getCmp('foreingResidence').allowBlank = true;
					                                    break;
					                                case 'loadlegalExistence':
					                                	Ext.getCmp('legalExistence').allowBlank = true;
					                                    break;
					                                    
					                                  default:
					                                    break;
					                                
						                            }
					                            }
					                            
											}
											
											Ext.getCmp('hrefFileList').update(files);
										},
										failure : function() {
										}
									});
								
								Ext.Ajax.request({
									url : 'approval/getLog.action',
									method : 'POST',
										params : {
											supplierId : rec.get('id')
										},
										success : function(response,opts) {
											Ext.getCmp('approvalLog').update(table);
											response = Ext.decode(response.responseText);
											var index = 0;
											var table = "<table class='logTable'><tr><th>Usuario</th><th>Acción</th><th>Fecha</th><th>Notas</th></tr>";
											for (index = 0; index < response.data.length; index++) {
												table = table + "<tr>";
												table = table + "<td>" + response.data[index].userName + "</td>" ;
												table = table + "<td>" + response.data[index].action + "</td>" ;
												table = table + "<td style='width:15%;'>" + Ext.util.Format.date(new Date(response.data[index].recordDate),'d-m-Y H:i:s') + "</td>" ;
												table = table + "<td style='width:70%;text-align:left;'>" + response.data[index].notes + "</td>" ;
												table = table + "</tr>";
											}
											var table = table + "</table>";
											Ext.getCmp('approvalLog').update(table);
										},
										failure : function() {
										}
									});*/
						    	box.hide();
							}
					    }
					});
		    	}else{
		    		box.hide();
		    		if(!me.mainPanel){
						me.mainPanel = Ext.create('Ext.panel.Panel', {
					        autoWidth: true,
					        renderTo: 'content',
					        layout:'fit',
					        border:false,
					        frame:false,
					        items: [{
					        	xtype: 'supplierForm'
							}]
						});
					}
		    		
		    		var rfcValid = document.getElementById("rfcValid").value;
		    		
		    		if(rfcValid.includes("_APM_")) rfcValid = rfcValid.replace("_APM_", "&");
		    		
			    	Ext.getCmp('supRfc').setValue(rfcValid);
			    	
			    	var razonSocialValid = document.getElementById("razonSocialValid").value;
			    	
			    	Ext.getCmp('razonSocial').setValue(razonSocialValid);
			    	
			    	var searchTypeValid = document.getElementById("searchTypeValid").value;
			    	
			    	Ext.getCmp('searchType').setValue(searchTypeValid);
			    	
			    	var approvalFlowUserValid = document.getElementById("approvalFlowUserValid").value;
			    	
			    	Ext.getCmp('nombreContactoCxC').setValue(approvalFlowUserValid);
			    	
			    	Ext.getCmp('dataTax').hide();
		    	}
		    	
				/*if(!me.mainPanel){
					me.mainPanel = Ext.create('Ext.panel.Panel', {
				        autoWidth: true,
				        height:500,
				        renderTo: 'content',
				        layout:'fit',
				        border:false,
				        frame:false,
				        items: [{
				        	xtype: 'supplierForm'
						}]
					});
				}*/
		    }
		});
		
		
	}
});	