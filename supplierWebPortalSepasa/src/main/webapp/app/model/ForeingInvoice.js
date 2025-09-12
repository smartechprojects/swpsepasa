Ext.define('SupplierApp.model.ForeingInvoice', {
    extend: 'Ext.data.Model',
    fields: [
			{name:  'addressNumber'},
			{name:  'orderNumber'},
			{name:  'orderType'},
			{name:  'voucherType'},
			{name:  'country'},
			{name:  'expeditionDate', type: 'date', dateFormat: 'c'},
			{name: 'receptCompany'},
			{name: 'foreignCurrency'},
			{name:  'foreignSubtotal'},
			{name:  'foreignRetention'},
			{name:  'foreignDebit'},
			{name:  'foreignTaxes'},
			{name:  'foreignDescription'},
			{name:  'foreignNotes'},
			{name:  'usuarioImpuestos'},
			{name:  'invoiceNumber'},
			{name:  'uuid'},
			{name:  'taxId'},
			{name: 'receiptIdList'}
        ],
        proxy: {
            type: 'ajax',
            api: {
                create: 'supplier/orders/saveForeign.action',
                update: 'supplier/orders/acceptForeign.action',
                destroy: 'supplier/orders/rejectForeign.action'
            },
            reader: {
                type: 'json',
                root: 'data',
                successProperty: 'success'
            },
            writer: {
            	type: 'json',
                writeAllFields: true,
                encode: false
            }
        },
        root: {
            expanded: false
        }
});