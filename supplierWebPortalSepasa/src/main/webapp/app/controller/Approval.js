Ext.define('SupplierApp.controller.Approval', {
    extend: 'Ext.app.Controller',
    stores: ['Approval'],
    models: ['SupplierDTO','Supplier'],
    views: ['approval.ApprovalGrid','approval.ApprovalPanel','supplier.SupplierForm',
    		'approval.ApprovalSearchGrid','approval.ApprovalSearchPanel'],
    refs: [{
        	ref: 'approvalGrid',
        	selector: 'approvalGrid'
	    },{
        	ref: 'supplierForm',
        	selector: 'supplierForm'
	    },
	    {
        	ref: 'approvalSearchGrid',
        	selector: 'approvalSearchGrid'
	    }],
 
    init: function() {
    	this.supDetailWindow = null;
        this.control({
				'#approveSupplier' : {
					"buttonclick" : this.approveSupplier
				},
				'#rejectSupplier' : {
					"buttonclick" : this.rejectSupplier
				},
	            'approvalGrid': {
	            	itemdblclick: this.gridSelectionChange
	            },
	            '#deleteSupplier' : {
					"buttonclick" : this.deleteSupplier
				},
				'approvalSearchGrid button[action=searchAppSupplier]' : {
					click : this.searchAppSupplier
				},
				'#openAppNotes' : {
					"buttonclick" : this.openAppNotes
				},
				});
    },
    
    openAppNotes : function(grid, rowIndex, colIndex, record) {
    	var me = this;
    	var record = grid.store.getAt(rowIndex);
    	
     	var notesWindow = new Ext.Window({
    		layout : 'fit',
    		title : 'Notas aprobacion y rechazo',
    		width : 600,
    		height : 260,
    		modal : true,
    		closeAction : 'destroy',
    		resizable : false,
    		minimizable : false,
    		maximizable : false,
    		autoScroll: true,
    		plain : true,
    		html: SuppAppMsg.approvalApprove + '<br><br>' + record.data.approvalNotes + '<br><br>' +
    				SuppAppMsg.approvalReject + '<br><br>' + record.data.rejectNotes
    	});
     	
     	notesWindow.show();
    },
    
    approveSupplier : function(grid, rowIndex, colIndex, record) {
    	var record = grid.store.getAt(rowIndex);
    	var dto = Ext.create('SupplierApp.model.SupplierDTO',record.data);
    	var currentApprover = addressNumber;
    	var status ="APROBADO";
    	var step = record.data.approvalStep;
    	
    	Ext.Ajax.request({
			url : 'supplier/getById.action',
			method : 'GET',
				params : {
					start : 0,
					limit : 15,
					id:record.data.id
				},
				success : function(response,opts) {
				
					response = Ext.decode(response.responseText);
					supp = response.data;
					if(step == 'THIRD'){
			    		if(supp.glClass ==""||
						   supp.glClass ==null){
							Ext.MessageBox.alert({ maxWidth: 700, minWidth: 650, title: SuppAppMsg.supplierMsgValidationLoad , msg:  'Existen errores en el formato. Por favor verifique los campos habilitados antes de enviar su solicitud.' });
							return false;
						}
			    	}
			    	
					var dlgAccept = Ext.MessageBox.show({
						title : SuppAppMsg.approvalAceptSupp,
						msg : SuppAppMsg.approvalNoteAcept,
						buttons : Ext.MessageBox.YESNO,
						multiline: true,
						width:500,
						buttonText : {
							yes : SuppAppMsg.approvalAcept,
							no : SuppAppMsg.approvalExit
						},
						fn : function(btn, text) {
							if (btn === 'yes') {
								if(text != ""){
									var box = Ext.MessageBox.wait(
											SuppAppMsg.approvalUpdateData,
											SuppAppMsg.approvalExecution);
									var notes = text;
									Ext.Ajax.request({
									    url: 'approval/update.action',
									    method: 'POST',
									    params: {
								        	currentApprover:currentApprover,
								            status:status,
								            step:step,
								            notes:notes
								        },
									    jsonData: dto.data,
									    success: function(fp, o) {
									    	var res = Ext.decode(fp.responseText);
									    	grid.store.load();
									    	box.hide();
									    	if(res.message == "Success"){
									    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.approvalRespAprobadoSucc);
									    	}else if(res.message == "Error JDE"){
									    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.approvalRespErrorJDE);
									    	}else if(res.message == "Succ Update"){
									    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
									    				SuppAppMsg.approvalRespUpdateSupp + " " + o.jsonData.addresNumber+"<br><br>");
									    	}else if(res.message == "Rejected"){
									    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
									    				SuppAppMsg.approvalRespRejected + " " + o.jsonData.ticketId);
									    	}
								        	//Ext.Msg.alert('Respuesta', res.message);
									    },
									    failure: function() {
									    	box.hide();
									    	Ext.MessageBox.show({
								                title: 'Error',
								                msg: SuppAppMsg.approvalUpdateError,
								                buttons: Ext.Msg.OK
								            });
									    }
									}); 
									
									
								}else{
			            			Ext.Msg.alert(SuppAppMsg.approvalAlert, SuppAppMsg.approvalMessages);
			            		}

							}
						}
					});
			    	
			    	dlgAccept.textArea.inputEl.set({
					    maxLength: 1999
					});
			    	
					
				},
				failure : function() {
					 box.hide();
				}
			});
    	
    },
    
    rejectSupplier : function(grid, rowIndex, colIndex, record) {
    	var record = grid.store.getAt(rowIndex);
    	var dto = Ext.create('SupplierApp.model.SupplierDTO',record.data);
    	var currentApprover = addressNumber;
    	var status ="RECHAZADO";
    	var step = record.data.approvalStep;
    	
    	var dlgRejected = Ext.MessageBox.show({
			title : SuppAppMsg.approvalAceptSupp,
			msg : SuppAppMsg.approvalNoteReject,
			buttons : Ext.MessageBox.YESNO,
			maxLength : 255,
			enforceMaxLength : true,
			multiline: true,
			width:500,
			buttonText : {
				yes : SuppAppMsg.approvalAcept,
				no : SuppAppMsg.approvalExit
			},
			fn : function(btn, text) {
				if (btn === 'yes') {
					if(text != ""){
						var box = Ext.MessageBox.wait(
								SuppAppMsg.approvalUpdateData,
								SuppAppMsg.approvalExecution);
						var notes = text;
						Ext.Ajax.request({
						    url: 'approval/update.action',
						    method: 'POST',
						    params: {
					        	currentApprover:currentApprover,
					            status:status,
					            step:step,
					            notes:notes
					        },
						    jsonData: dto.data,
						    success: function(fp, o) {
						    	var res = Ext.decode(fp.responseText);
						    	grid.store.load();
						    	box.hide();
						    	if(res.message == "Success"){
						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.approvalRespAprobadoSucc);
						    	}else if(res.message == "Error JDE"){
						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, SuppAppMsg.approvalRespErrorJDE);
						    	}else if(res.message == "Succ Update"){
						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
						    				SuppAppMsg.approvalRespUpdateSupp + " " + o.jsonData.addresNumber);
						    	}else if(res.message == "Rejected"){
						    		Ext.Msg.alert(SuppAppMsg.approvalResponse, 
						    				SuppAppMsg.approvalRespRejected + " " + o.jsonData.ticketId);
						    	}
						    },
						    failure: function() {
						    	box.hide();
						    	Ext.MessageBox.show({
					                title: 'Error',
					                msg: SuppAppMsg.approvalUpdateError,
					                buttons: Ext.Msg.OK
					            });
						    }
						}); 
						
						
					}else{
            			Ext.Msg.alert(SuppAppMsg.approvalAlert, SuppAppMsg.approvalMessages);
            		}

				}
			}
		});
    	
    	dlgRejected.textArea.inputEl.set({
		    maxLength: 1999
		});
    	
    },
    
    gridSelectionChange: function(model, record) {
        if (record) {
        	var box = Ext.MessageBox.wait(SuppAppMsg.approvalLoadRegistrer , SuppAppMsg.approvalExecution);
        	var me = this;
        	me.supDetailWindow = new Ext.Window({
        		layout : 'fit',
        		title : SuppAppMsg.approvalDetailsSupplier ,
        		width : 1200,
        		height : 550,
        		modal : true,
        		closeAction : 'destroy',
        		resizable : true,
        		minimizable : false,
        		maximizable : false,
        		plain : true,
        		items : [ {
        			xtype : 'supplierPanelDetail',
        			border : true,
        			height : 415
        		} ]

        	});
        	me.supDetailWindow.show();
        	var form = this.getSupplierForm().getForm();
			
        	Ext.Ajax.request({
				url : 'supplier/getById.action',
				method : 'GET',
					params : {
						start : 0,
						limit : 15,
						id:record.data.id
					},
					success : function(response,opts) {
					
						response = Ext.decode(response.responseText);
						rec = Ext.create('SupplierApp.model.Supplier');
						rec.set(response.data);
			        	form.loadRecord(rec);
			        	var approvarSupplier = response.data.approvalStep;
			        	var contrib = response.data.fisicaMoral;
			        	var countryData = response.data.country;
			        	var approvalStep = response.data.approvalStep;
			        	var approvalStatus = response.data.approvalStatus;
			        	
			        	form.findField('codigoPostal').setRawValue(response.data.codigoPostal);
			        	form.findField('regionesTmp').setValue(response.data.regiones);
			        	form.findField('categoriasTmp').setValue(response.data.categorias);
			        	
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
			        	Ext.getCmp('ticketForSearch').setValue(rec.data.ticketId);
			        	
			        	
			        	Ext.getCmp('docLoadSection').hide();
			        	Ext.getCmp('searchTicket').hide();
			        	Ext.getCmp('searchCP').hide();
			        	Ext.getCmp('updateTaxData').hide();
			        	
			        	/*Ext.getCmp('dataTax').show();
				
				        form.findField('rfcDocument').allowBlank = true;
		        		form.findField('domDocument').allowBlank = true;
		        		form.findField('edoDocument').allowBlank = true;
		        		//form.findField('escDocument').allowBlank = true;
		        		//form.findField('notDocument').allowBlank = true;
		        		form.findField('identDocument').allowBlank = true;*/
		        		
		        		/*form.findField('ticketForSearch').setReadOnly(true);
		        		form.findField('addresNumber').setReadOnly(true);
		        		form.findField('razonSocial').setReadOnly(true);
		        		form.findField('country').setReadOnly(true);
		        		//form.findField('currencyValidation').setReadOnly(true);
		        		form.findField('fisicaMoral').setReadOnly(true);
		        		form.findField('emailSupplier').setReadOnly(true);
		        		form.findField('calleNumero').setReadOnly(true);
		        		form.findField('delegacionMnicipio').setReadOnly(true);
		        		form.findField('codigoPostal').setReadOnly(true);
		        		form.findField('colonia').setReadOnly(true);
		        		form.findField('estado').setReadOnly(true);
		        		form.findField('telefonoDF').setReadOnly(true);
		        		form.findField('faxDF').setReadOnly(true);
		        		form.findField('emailComprador').setReadOnly(true);
		        		//form.findField('apellidoPaternoCxC').setReadOnly(true);
		        		//form.findField('apellidoMaternoCxC').setReadOnly(true);
		        		//form.findField('faxCxC').setReadOnly(true);
		        		form.findField('cargoCxC').setReadOnly(true);
		        		form.findField('nombreCxP01').setReadOnly(true);
		        		form.findField('emailCxP01').setReadOnly(true);
		        		form.findField('telefonoCxP01').setReadOnly(true);
		        		form.findField('nombreCxP02').setReadOnly(true);
		        		form.findField('emailCxP02').setReadOnly(true);
		        		form.findField('telefonoCxP02').setReadOnly(true);
		        		form.findField('nombreCxP03').setReadOnly(true);
		        		form.findField('emailCxP03').setReadOnly(true);
		        		form.findField('telefonoCxP03').setReadOnly(true);
		        		form.findField('nombreCxP04').setReadOnly(true);
		        		form.findField('emailCxP04').setReadOnly(true);
		        		form.findField('telefonoCxP04').setReadOnly(true);
		        		form.findField('tipoIdentificacion').setReadOnly(true);
		        		form.findField('numeroIdentificacion').setReadOnly(true);
		        		form.findField('nombreRL').setReadOnly(true);
		        		form.findField('apellidoPaternoRL').setReadOnly(true);
		        		form.findField('apellidoMaternoRL').setReadOnly(true);*/
		        		
		        		if(contrib == 'F'){
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
		        				        		
		        		/*if(approvarSupplier == 'FIRST'){
		        			form.findField('addresNumber').setReadOnly(false);
		        			form.findField('supplierWithOC').setReadOnly(false);
		        			form.findField('supplierWithoutOC').setReadOnly(false);	 	        			
		        			form.findField('catCode15').setReadOnly(false);
			        		form.findField('catCode27').setReadOnly(false);
			        		form.findField('industryClass').setReadOnly(false);
		        		}else{
		        			
		        			form.findField('catCode15').setReadOnly(true);
			        		form.findField('catCode27').setReadOnly(true);
			        		form.findField('industryClass').setReadOnly(true);
			        		//form.findField('glClass').setReadOnly(true);
		        		}*/
		        		
		        		/*if(approvarSupplier == 'SECOND'){*/
		        			//form.findField('pmtTrmCxC').setReadOnly(false);
		        		/*}else{
		        			form.findField('pmtTrmCxC').setReadOnly(true);
		        		}*/
		        		
		        		if(countryData == 'MX'){
		        			Ext.getCmp('supRfc').allowBlank=false;
		        			Ext.getCmp('supRfc').show();
		        			Ext.getCmp('taxId').allowBlank=true;
		        			Ext.getCmp('taxId').hide();
		        			
		        			Ext.getCmp('fldColonia').show();
			    			Ext.getCmp('fldColonia').allowBlank=false;
			    			Ext.getCmp('coloniaEXT').hide();
			    			Ext.getCmp('coloniaEXT').allowBlank=true;
			    			
			    			Ext.getCmp('fldMunicipio').show();
							Ext.getCmp('fldMunicipio').allowBlank=false;
		        		}else{
		        			Ext.getCmp('taxId').allowBlank=false;
		        			Ext.getCmp('taxId').show();
		        			Ext.getCmp('supRfc').allowBlank=true;
		        			Ext.getCmp('supRfc').hide();
		        			
		        			Ext.getCmp('coloniaEXT').allowBlank=false;
			    			Ext.getCmp('coloniaEXT').show();
			    			Ext.getCmp('coloniaEXT').setValue(response.data.colonia);
			    			
			    			Ext.getCmp('fldColonia').hide();
			    			Ext.getCmp('fldColonia').allowBlank=true;
			    			
			    			Ext.getCmp('fldMunicipio').hide();
							Ext.getCmp('fldMunicipio').allowBlank=true;
		        		}
		        		
		        		
		        		form.findField('nombreContactoCxC').setReadOnly(true);
		        		//form.findField('telefonoContactoCxC').setReadOnly(true);
		        		

						if (((role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT') && approvalStep == 'FIRST' && approvalStatus == 'PENDIENTE') ||
							(role == 'ROLE_ADMIN')) {

							form.findField('outSourcing').setReadOnly(false);

						}

		        		//form.findField('currencyCode').setValue(rec.data.currencyCode); 
		        		
		        		Ext.getCmp('updateSupplierForm').show();
		        		
		        		if(role=='ROLE_ADMIN' ||role=='ROLE_CXP' ||role=='ROLE_CXP_IMPORT' ||role == 'ROLE_PURCHASE' || role == 'ROLE_ADMIN_PURCHASE' || role=='ROLE_PURCHASE_IMPORT' ){
		        			
		        			Ext.getCmp('updateSupplierForm').show();
		        		}
		        		
		        		form.getFields().each(function(field) {
			        	      field.setReadOnly(true);
			        	    });
		        		
		        		form.findField('searchType').setReadOnly(false);
		        		
		        		if(rec.data.approvalStep == 'THIRD'){
		        			//Ext.getCmp('dataTax').show();
		        			
		        			Ext.getCmp('dataTax').query('textfield').forEach(function(field) {
		        			    field.setReadOnly(false);
		        			});
		        			
		        			form.findField('creditMessage').setReadOnly(true);
		        		}else{
		        			//Ext.getCmp('dataTax').hide();
		        			form.findField('dischargeApplicant').setReadOnly(false);
		        			
		        			//if(rec.data.approvalStep == 'FIRST') 
		        			form.findField('creditMessage').setReadOnly(false);
		        		}
		        		
		        		
		        		

			        	/*Ext.Ajax.request({
							url : 'documents/listDocumentsByOrder.action',
							method : 'GET',
								params : {
									start : 0,
									limit : 15,
									orderNumber:100,
									orderType:'GRAL',
									addressNumber:'GENERAL'
								},
								success : function(response,opts) {
									response = Ext.decode(response.responseText);
									var data = response.data;
									var hrefList = "";
									for (var i = 0; i < data.length; i++) {
										href = "documents/openDocument.action?id=" + data[i].id;
										fileHref = "*** <a href= '" + href + "' target='_blank'>" +  data[i].name + "</a>";
										hrefList = "<p>"  + hrefList + fileHref + "</p>";
					    			}
									Ext.getCmp('internalFileList').setValue(hrefList);
									

								},
								failure : function(response,opts) {
									box.hide();
								}
							});*/
			        	
						box.hide();
					},
					failure : function() {
						 box.hide();
					}
				});
        }
    },
    
    deleteSupplier : function(grid, rowIndex, colIndex, record) {
    	var record = grid.store.getAt(rowIndex);
    	var id = record.data.id;
    	
    	var gridX = this.getApprovalSearchGrid();
    	var store = gridX.getStore();
    	//var dto = Ext.create('SupplierApp.model.SupplierDTO',record.data);
    	
    	Ext.MessageBox.show({
            title:window.navigator.language.startsWith("es", 0)? 'Eliminar ticket':'Delete ticket',
            msg: window.navigator.language.startsWith("es", 0)? '¿Desea eliminar el ticket?':'Do you want to delete the ticket?',
            buttons: Ext.MessageBox.YESNO,
            fn: function showResult(btn){
                if(btn == 'yes'){
                	var box = Ext.MessageBox.wait(
                			SuppAppMsg.supplierWaitSeconds,
							SuppAppMsg.approvalExecution);
					
					Ext.Ajax.request({
					    url: 'public/deleteSupplier.action',
					    method: 'POST',
					    params: {
					    	id:id
				        },
					    success: function(fp, o) {
					    	var res = Ext.decode(fp.responseText);
					    	var file = store.findRecord('id',id);
							if(file != null)store.removeAt(store.find('id', id))
					    	gridX.getView().refresh();
					    	box.hide();
					    	if(res.message == "delete_supp_succ"){
					    		Ext.Msg.alert(SuppAppMsg.approvalResponse,
					    		window.navigator.language.startsWith("es", 0)? 'Solicitud eliminada exitosamente.':'Request successfully deleted.');
					    	}
					    },
					    failure: function() {
					    	box.hide();
					    	Ext.MessageBox.show({
				                title: 'Error',
				                msg: window.navigator.language.startsWith("es", 0)? 'Surgió un problema en eliminar la solicitud.':'There was a problem deleting the request.',
				                buttons: Ext.Msg.OK
				            });
					    }
					}); 
               	 
                }
            },
            icon: Ext.MessageBox.QUESTION
        });
    	
    },
    
    searchAppSupplier: function(button) {
    	var grid = this.getApprovalSearchGrid();
    	var store = grid.getStore();
    	var box = Ext.MessageBox.wait(SuppAppMsg.supplierProcessRequest, SuppAppMsg.approvalExecution);

       	var ticketId = Ext.getCmp('supSearchTicket').getValue();
    	var approvalStep = Ext.getCmp('supSearchApprLevel').getValue();
    	var approvalStatus = Ext.getCmp('supSearchTicketSts').getValue();
    	var currentApprover = Ext.getCmp('supSearchApprover').getValue();
    	/*
    	var fechaAprobacion = Ext.getCmp('fechaAprobacion').getValue();
    	
    	*/
    	var name = Ext.getCmp('supSearchName').getValue();
    	store.loadData([], false);
    	grid.getView().refresh();
    	
    	Ext.Ajax.request({
			url : 'approval/search.action',
			method : 'GET',
				params : {
					ticketId:ticketId,
					approvalStep:approvalStep,
					approvalStatus:approvalStatus,
					fechaAprobacion:'',
					currentApprover:currentApprover,
					name:name
				},
				success : function(response,opts) {
					response = Ext.decode(response.responseText);
					if(response.data != null){
						for(var i = 0; i < response.data.length; i++){
				    		var r = Ext.create('SupplierApp.model.SupplierDTO',response.data[i]);
				    		store.insert(i, r);
						}
					}
						
					box.hide();
					},
					failure : function() {
						 box.hide();
					}
				});
 
    }
});