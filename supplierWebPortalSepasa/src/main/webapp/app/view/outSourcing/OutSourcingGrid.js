Ext.define('SupplierApp.view.outSourcing.OutSourcingGrid' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.outSourcingGrid',
    loadMask: true,
    forceFit: true,
	cls: 'extra-large-cell-grid', 
	store:'OutSourcingDocument',
	viewConfig: {
		stripeRows: true,
		style : { overflow: 'auto', overflowX: 'hidden' },
		getRowClass: function(record, rowIndex, rowParams, store) {

			if(record.get('docStatus') == 'RECHAZADO'){
				return 'alertOrange';
			}
        }
	},
    initComponent: function() {
    	
    	var statusStore = Ext.create('Ext.data.ArrayStore', {
    	    fields: ['id', 'value'],
    	    data :[['APROBADO', 'Aprobado'],['PENDIENTE', 'Pendiente'],['RECHAZADO', 'Rechazado']]
    	});

    	var documentType = Ext.create('Ext.data.ArrayStore', {
    	    fields: ['name', 'type'],
    	    data :[['REGIS_REN_REPSE', 'Registro y/o renovacion REPSE'],
    	    	['LIST_PERS', 'Listado de Personal'],
    	    	['CFDIS', 'CFDIs'],
    	    	['OPI_CUMP_SAT', 'Opinion de cumplimiento SAT'],
    	    	['OPI_CUMP_IMSS', 'Opinion de cumplimiento IMSS'],
    	    	['OPI_CUMP_INFO', 'Opinion de cumplimiento INFONAVIT'],
    	    	['CED_CUOT_IMSS', 'Cedula de Determinacion de cuotas IMSS'],
    	    	['LIQ_CUOT_IMSS', 'Resumen de Liquidacion de cuotas IMSS'],
    	    	['CAPT_PAG_CUOT_IMSS', 'Linea de Captura para pago de cuotas IMSS'],
    	    	['COMP_PAG_CUOT_IMSS', 'Comprobante de pago de cuotas IMSS'],
    	    	['DEC_PAG_PROV_IVA', 'Declaracion de pago provisional mensual de IVA'],
    	    	['CAPT_PAG_PROV_IVA', 'Acuse de Recibo y Linea de Captura de pago provisional mensual de IVA'],
    	    	['COMP_PAG_PROV_IVA', 'Comprobante de pago provisional mensual de IVA'],
    	    	['DECL_MENS_ISR', 'Declaracion mensual de retenciones de ISR asalariados'],
    	    	['CAPT_PAG_ISR', 'Acuse de Recibo y Linea de Captura de retencion mensual de ISR asalariados'],
    	    	['COMP_PAG_ISR', 'Comprobante de pago de retenciones de ISR asalariados'],
    	    	
    	    	['CED_CUOT_INFO', 'Cedula de Determinacion de cuotas INFONAVIT'],
    	    	['LIQ_CUOT_INFO', 'Resumen de Liquidacion de cuotas INFONAVIT'],
    	    	['CAPT_PAG_CUOT_INFO', 'Linea de Captura para pago de cuotas INFONAVIT'],
    	    	['COMP_PAG_CUOT_INFO', 'Comprobante de pago de cuotas INFONAVIT'],
    	    	['DECL_IMSS_ICSOE', 'Acuse de Declaracion cuatrimestral (IMSS-ICSOE)'],
    	    	['DECL_INFO_SISUB', 'Acuse de Declaracion cuatrimestral (INFONAVIT-SISUB)'],/*['PAGO_PROV_IVA', 'Pago Provisional de IVA'],['CONST_CUMP_OBLIG', 'Const de cumpl de obligaciones'],
    	    	   ['CONST_SIT_FIS', 'Constancia de Sit Fiscal '],['AUT_STPS', 'Autorización STPS'],
    	    	   ['CEDU_DETERM_CUOTAS', 'Cedula de Determinacion de Cuotas'],
    	    	   /*['LISTA_TRAB', 'Lista de trabajadores'],['DECL_MENS_IMSS', 'Decl inf del IMSS'],
    	    	   ['PROV_ISR_SAL', 'Pagos Provisionales de ISR'],['CUOT_OBR_PATR', 'Cuotas obrero-patronales al IMSS'],
    	    	   ['APOR_FON_NAL_VIV', 'Aportaciones INFONAVIT'],/*['PAGO_ISN', 'Pago de ISN mensual'],
    	    	   ['REC_NOMINA', 'Recibos de Nómina']*/]
    	});    	
    	
    	var monthLoad = Ext.create('Ext.data.ArrayStore', {
    	    fields: ['name', 'month'],
    	    data :[['&nbsp;', null],['ENERO', 1],['FEBRERO', 2],
    	    	   ['MARZO', 3],['ABRIL',4],
    	    	   ['MAYO', 5],['JUNIO', 6],
    	    	   ['JULIO', 7],['AGOSTO', 8],
    	    	   ['SEPTIEMBRE', 9],['OCTUBRE', 10],
    	    	   ['NOVIEMBRE', 11],['DICIEMBRE', 12]]
    	});
    	
        var yearsStore = Ext.create('Ext.data.ArrayStore', {
	          fields : ['years'],
	          //data : years
	         //data : [['&nbsp;'],[years]]
	          data : [['2025'],['2024'],['2023'],['2022']]
	    });
    	
        this.columns = [
       {
            hidden:true,
            dataIndex: 'id',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
       },{
            text     : 'Fecha de Carga',
            width: 110,
            dataIndex: 'uploadDate',
            renderer: function(date){
                return Ext.Date.format((new Date(date)),'d-M-Y  H:i');
            }	
        },{
            text     : 'Proveedor',
            width: 210,
            dataIndex: 'supplierName'
        },
        
        
	    {
	        text     : 'No Proveedor',
	        width: 130,
	        dataIndex: 'addressBook'
	
	    },{
          text     : 'Periodo',
          width: 120,
         // xtype:'templatecolumn', 
         // tpl:'{monthLoad} {yearLoad}',
          renderer: function(value,metaData,record,store,view){
          
          	var month = record.data.monthLoad;
          	var year = record.data.yearLoad;
          	if(month != 0){
          		return monthToString(month) + " " + year;
          	}else
          		{
          		return "";
          		}
              
          }	
      },{
            text     : 'Archivo',
            width: 220,
            dataIndex: 'name'
        },{
        	text     : 'Abrir',
            width: 40,
            renderer: function (value, meta, record) {
            	var href = "supplier/openOSDocument.action?id=" + record.data.id;
                return '<a href="' + href + '" target="_blank">'+ 'Abrir' +'</a>';
            }
        },{
            text     : 'Tipo',
            width: 100,
            dataIndex: 'documentType'
        },{
            text     : 'UUID',
            width: 100,
            hidden:true,
            dataIndex: 'uuid',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
        },{
            text     : 'Frecuencia',
            width: 100,
            dataIndex: 'frequency',
            renderer: function(value, meta, record) {
            	            	
            	switch (value) {
            	  case 'INV':
            	    return "Facturación";
            	    break;
            	  case 'MONTH':
              	    return "Mensual";
              	    break;
            	  case 'SECOND':
            	    return "Bimestral";
            	    break;
            	  case 'QUARTER':
            	    return "Cuatrimestral";
            	    break;
            	  case 'BL':
              	    return "Inicial";
              	    break;
              	  default:
              		break;
            	}

             }
        },{
            text     : 'Estado',
            width: 100,
            dataIndex: 'docStatus'
        },{
            xtype: 'actioncolumn', 
            width: 40,
            header: 'Notas',
            align: 'center',
			name : 'openOSNotes',
			itemId : 'openOSNotes',
            style: 'text-align:center;',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
            items: [
            	{
            	icon:'resources/images/notepad.png',
          	     iconCls: 'increaseSize',
            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  if(r.data.notes == null || r.data.notes == '') {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }
              	  },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]
        },{
        	xtype: 'actioncolumn', 
            hidden:role=='ROLE_ADMIN' || role.includes('ROLE_RH') || role.includes('ROLE_TAX') || role.includes('ROLE_LEGAL') || role.includes('ROLE_3RDPARTY')?false:true,
            width: 90,
            header: SuppAppMsg.approvalReject,
            align: 'center',
			name : 'rejectOSDoc',
			itemId : 'rejectOSDoc',
            style: 'text-align:center;',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
            items: [
            	{
            	  icon:'resources/images/close.png',
            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  if(r.data.docStatus != "PENDIENTE") {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }
              	  },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]
        },{
        	xtype: 'actioncolumn', 
        	hidden:role=='ROLE_ADMIN' || role.includes('ROLE_RH') || role.includes('ROLE_TAX') || role.includes('ROLE_LEGAL') || role.includes('ROLE_3RDPARTY')?false:true,
            width: 90,
            header: SuppAppMsg.approvalApprove,
            align: 'center',
			name : 'approveSODoc',
			itemId : 'approveSODoc',
            style: 'text-align:center;',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
            items: [
            	{
            		icon:'resources/images/accept.png',
            	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  if(r.data.docStatus != "PENDIENTE") {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }
              	   },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]
        },{
        	xtype: 'actioncolumn', 
            hidden:role.includes('ROLE_SUPPLIER')?false:true,
            width: 80,
            header: 'Cargar reemplazo',
            align: 'center',
			name : 'uploadNewSODoc',
			itemId : 'uploadNewSODoc',
            style: 'text-align:center;',
            hideable: false,//Para que no aparezca en la lista de "Columnas"
            items: [
            	{
            	  icon:'resources/images/file.png',
              	  getClass: function(v, metadata, r, rowIndex, colIndex, store) {
              		  
              		  if(r.data.documentType == 'REC_NOMINA'){
              			return "x-hide-display";
              		  }

              		  if(r.data.docStatus != "RECHAZADO") {
        	              return "x-hide-display";
        	          }else{
        	        	  return "increaseSize";
        	          }
              	   },
                  handler: function(grid, rowIndex, colIndex) {
                  	this.fireEvent('buttonclick', grid, rowIndex, colIndex);
             }}]
        }];
        
   	 this.dockedItems = [{
		    xtype: 'toolbar',
		    dock: 'top',
		    items: [
		    	/*{
		            xtype: 'datefield',
		            labelWidth: 30,
		            width:100,
		            fieldLabel: SuppAppMsg.purchaseOrderDesde,
		            id: 'fromDateOS',
		            margin:'10 0 0 10',
		            dateFormat: 'Y-M-d',
		            labelAlign:'top',
		            maxValue: new Date()
		        },{
		            xtype: 'datefield',
		            labelWidth: 30,
		            width:100,
		            margin:'10 0 0 30',
		            dateFormat: 'Y-M-d',
		            fieldLabel: SuppAppMsg.purchaseOrderHasta,
		            id: 'toDateOS',
		            labelAlign:'top',
		            maxValue: new Date()
		        },*/		    	{
					xtype : 'combo',
					fieldLabel : SuppAppMsg.supplierForm58,
					id : 'periodMonth',
					name : 'periodMonth',
					store : monthLoad,
					valueField : 'month',
					displayField: 'name',
	                typeAhead: true,
	                minChars: 2,
	                triggerAction: 'all',
	                labelWidth:30,
	                width : 100,
	                labelAlign:'top',
	                fieldStyle: 'font-size:10px;',
	                margin:'0 20 0 25',
	                editable: false,
	                listeners: {
	                    select: function (comp, record, index) {
	                    	
	                        if (comp.getValue() === null) {
	                            comp.setRawValue('');
	                            comp.setValue(null);
	                        }
	                    }
	                }

			},{
				xtype : 'combo',
				fieldLabel : SuppAppMsg.supplierForm59,
				id : 'periodYear',
				name : 'periodYear',
				store : yearsStore,
				valueField : 'years',
				displayField: 'years',
                typeAhead: true,
                minChars: 2,
                triggerAction: 'all',
                labelWidth:30,
                width : 100,
                labelAlign:'top',
                fieldStyle: 'font-size:10px;',
                editable: false,
                listeners: {
                    select: function (comp, record, index) {
                    	
                        if (comp.getValue() === '&nbsp;') {
                            comp.setRawValue('');
                            comp.setValue(null);
                        }
                    }
                }
               

		},{
		            xtype: 'combo',
		            fieldLabel: 'Estado',
		            labelWidth : 40,
					width:150,
					store:statusStore,
		            valueField: 'id',
		            displayField: 'value',
		            margin:'10 0 0 20',
		            labelAlign:'top',
					id : 'docStatusOS'
		},{
			
            xtype: 'combo',
            fieldLabel: 'Tipo de archivo',
            labelWidth : 40,
			width:200,
            store: documentType,
            queryMode: 'local',
            displayField: 'type',
            valueField: 'name',
            margin:'0 0 0 30',
            labelAlign:'top',
			id : 'documentTypeOS',
			fieldStyle: 'font-size:10px;'
        },{	
					xtype: 'textfield',
		            fieldLabel: SuppAppMsg.suppliersName,
		            id: 'supNameOS',
		            width:200,
		            labelWidth:40,
		            labelAlign:'top',
		            margin:'10 0 0 20',
		            hidden:role=='ROLE_ADMIN' || role.includes('ROLE_TAX') || role.includes('ROLE_RH')|| role.includes('ROLE_LEGAL')|| role.includes('ROLE_REPSE') || role.includes('ROLE_3RDPARTY') || role.includes('ROLE_ADMIN_PURCHASE') || role.includes('ROLE_PURCHASE')?false:true,
            		listeners:{
						change: function(field, newValue, oldValue){
							field.setValue(newValue.toUpperCase());
						}
					}
				},{
					xtype: 'textfield',
		            id: 'supNumberOS',
		            width:200,
		            labelWidth:40,
		            labelAlign:'top',
		            margin:'0 20 0 5',
		            readOnly:true,
		            hidden:true,
		            value:role.includes('ROLE_SUPPLIER')?addressNumber:''
				},
			 {
		        xtype: 'button',
		        margin:'10 0 0 30',
		        text: SuppAppMsg.suppliersSearch,
		        iconCls:'icon-search',
		        itemId : 'searchDocsOS',
				id : 'searchDocsOS',
				action : 'searchDocsOS',
				cls: 'buttonStyle'
		    }]
		}]
   	 
		this.bbar = Ext.create('Ext.PagingToolbar', {
			store : 'OutSourcingDocument',
			displayInfo : true
		});
        
   	 	function monthToString(month){
   	 		var  months = ["","ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"];
   	 		return months[month]; 
   	 	};   	 
   	 
        this.callParent(arguments);
    }
});