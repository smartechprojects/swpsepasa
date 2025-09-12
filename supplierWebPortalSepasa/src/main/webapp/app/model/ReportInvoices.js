Ext.define('SupplierApp.model.ReportInvoices', {
    extend: 'Ext.data.Model',
    fields: [
				
				//{name:  'id'},
				
				{name:  'name'},
				{name:  'description'},
				{name:  'addressBook'},
				{name:  'documentNumber'},
				{name:  'documentType'},
				{name:  'status'},
				{name:  'size'},
				{name:  'fiscalType'},
				{name:  'type'},
				{name:  'uuid'},
				{name:  'folio'},
				{name:  'serie'},
				{name:  'fiscalRef'},
				
				{name:  'POHeaderId'},
				
				{name:  'razonSocial'},
				
				{name:  'rfcEmisor'},
				{name:  'rfcReceptor'},	
				{name:  'moneda'},
				{name:  'subtotal'},
				{name:  'impuestos'},
				{name:  'total'},
				{name:  'tipoCambio'},
				{name:  'descuento'},	
				{name:  'invoiceDate'},
				
				{name:  'paymentType'},
				{name:  'formaPago'},
				{name:  'compCargado'},
				{name:  'massive'},
				
				{name:  'uploadDate', type: 'date', dateFormat: 'c'},
				
				{name:  'accept'},
				
				{name:  'tipoComprobante'},
				{name:  'facturaPortal'},
				{name: 'orderNumber'},
				{name: 'razonSocialReceptor'}
        ]
});
