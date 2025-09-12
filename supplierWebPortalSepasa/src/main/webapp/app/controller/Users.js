Ext.define('SupplierApp.controller.Users', {
    extend: 'Ext.app.Controller',
    stores: ['Users'],
    models: ['Users'],
    views: ['users.UsersPanel','users.UsersForm','users.UsersGrid'],
    refs: [{
        	ref: 'usersForm',
        	selector: 'usersForm'
	    },
	    {
	        ref: 'usersGrid',
	        selector: 'usersGrid'
	    }],
 
    init: function() {
        this.control({
            'usersGrid': {
                selectionchange: this.gridSelectionChange
            },
            'usersForm button[action=saveUsers]': {
                click: this.saveUsers
            },
            'usersForm button[action=usersNew]': {
                click: this.resetUsersForm
            },
            'usersForm button[action=deleteUsers]': {
                click: this.deleteUsers
            },
            'usersForm button[action=updateUsers]': {
                click: this.updateUsers
            },
            '#searchUsers' : {
                "ontriggerclick": this.loadSearchList
            }
        });
    },

    gridSelectionChange: function(model, records) {
        if (records[0]) {
        	var form = this.getUsersForm().getForm();
        	var box = Ext.MessageBox.wait(SuppAppMsg.approvalLoadRegistrer, SuppAppMsg.approvalExecution);
        	form.loadRecord(records[0]);

        	var roleCombo = form.findField('userRole.id');
			roleCombo.store.load({
                callback: function (r, options, success) {
                		roleCombo.setValue(records[0].data.userRole.id);
                 }
            });		
			
        	var typeCombo = form.findField('userType.id');
        	typeCombo.store.load({
                callback: function (r, options, success) {
                	typeCombo.setValue(records[0].data.userType.id);
                 }
            });
        	
        	Ext.getCmp('usersFormUserName').setReadOnly(true);
        	Ext.getCmp('usersFormName').setReadOnly(true);
        	//Ext.getCmp('usersFormEmail').setReadOnly(true);
        	
            this.enableUpdate();
            box.hide();
        }
    },
    
    loadSearchList: function (event){
    	this.getUsersGrid().getStore().getProxy().extraParams={
    		query:event.getValue()
    	};
    	this.getUsersGrid().getStore().load();
    },
    
    saveUsers: function (button) {
    	var form = this.getUsersForm().getForm();
    	var grid = this.getUsersGrid();
    	if (form.isValid()) { 
        	var record = Ext.create('SupplierApp.model.Users');
        	values = form.getFieldValues();
        	updatedRecord = populateObj(record, values);
            
            if (values.id > 0){
            	Ext.Msg.alert( SuppAppMsg.usersSaveError, "Error");
    		} else{
    	    	var box = Ext.MessageBox.wait(SuppAppMsg.usersSaveDataMsj, SuppAppMsg.approvalExecution);
    	    	record.set(updatedRecord);
				record.save();
        	    grid.store.load();
        	    form.reset();
        	    box.hide();
    		}
    	}
    },
    
    resetUsersForm: function (button) {
    	var form = this.getUsersForm().getForm();
    	form.reset();
    	Ext.getCmp('usersFormUserName').setReadOnly(false);
    	Ext.getCmp('usersFormName').setReadOnly(false);
    	//Ext.getCmp('usersFormEmail').setReadOnly(false);
    	this.enableSave();
    },
        
    updateUsers: function (button) {
    	var form = this.getUsersForm().getForm();
    	var grid = this.getUsersGrid();
    	if (form.isValid()) { 
        	var record = form.getRecord();
        	values = form.getFieldValues();
        	updatedRecord = populateObj(record, values);

            if (values.id > 0){
    	    	var box = Ext.MessageBox.wait(SuppAppMsg.usersSaveDataMsj, SuppAppMsg.approvalExecution);
    	    	record.set(updatedRecord);
				record.save();
        	    grid.store.load();
        	    form.reset();
        	    this.enableSave();
        	    box.hide();
    		} else{
    			Ext.Msg.alert(SuppAppMsg.supplierUpdateFail, "Error");
    		}
    	}
    },
    
    deleteUsers: function (button) {
    	var form = this.getUsersForm().getForm();
    	var grid = this.getUsersGrid();
		record = form.getRecord();
		values = form.getFieldValues();
		if (values.id > 0){
			updatedRecord = populateObj(record, values);
			record.set(updatedRecord);
			this.getUsersStore().remove(record);
			this.getUsersStore().sync({callback: function() {},
                success: function() {
                	 grid.store.load();
            	     form.reset();
             },                            
             failure: function(batch, options) {
            	 Ext.Msg.alert(SuppAppMsg.usersSaveErrorTitle);
                }
            });
			this.getUsersStore().reload();
			form.reset();
		} else{
			Ext.Msg.alert(SuppAppMsg.usersDeleteError);
		}
    },

	enableUpdate: function(){
		Ext.getCmp('saveUsers').setDisabled(true);
		Ext.getCmp('updateUsers').setDisabled(false);
		Ext.getCmp('deleteUsers').setDisabled(false);	
	},

	enableSave: function(){
		Ext.getCmp('saveUsers').setDisabled(false);
		Ext.getCmp('updateUsers').setDisabled(true);
		Ext.getCmp('deleteUsers').setDisabled(true);
	},
	
	initController: function(){
	}
});