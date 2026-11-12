(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineAuditEventController', MachineAuditEventController);

    MachineAuditEventController.$inject = ['InitCrud', '$scope', 'MachineAuditEvent','machineId'];

    function MachineAuditEventController(InitCrud, $scope, MachineAuditEvent,machineId) {
        var vm = this;

        console.log("Machine audit "+machineId);
        vm.title = "Liste des informations d'audit";

        vm.columns = [{
            name: 'auditEventDate',
            title: 'Date',
            type: 'DateTime'
        }, {
            name: 'statut',
            title: 'Statut'
        }, {
            name: 'principal',
            title: 'Principale'
        }, {
            name: 'machineHostName',
            title: 'machineHostName'
        }];
        vm.actions = {
            view: false,
            edit: false,
            delete: false,
            add: false
        };

        var datas = {
            vm: vm,
            entityDatas: {
                statut: null,
                id: null
            },
            entity: MachineAuditEvent,
            addTemplateUrl: "machine-audit-event/machine-audit-event-dialog.html",
            deleteTemplateUrl: "machine-audit-event/machine-audit-event-delete-dialog.html",
            controller: "MachineAuditEvent",
            entityParamsItem: {
                machineId:machineId
            },
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();