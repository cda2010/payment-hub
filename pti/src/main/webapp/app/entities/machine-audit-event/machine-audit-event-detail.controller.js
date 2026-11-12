(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineAuditEventDetailController', MachineAuditEventDetailController);

    MachineAuditEventDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'MachineAuditEvent'];

    function MachineAuditEventDetailController($scope, $rootScope, $stateParams, previousState, entity, MachineAuditEvent) {
        var vm = this;

        vm.machineAuditEvent = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:machineAuditEventUpdate', function(event, result) {
            vm.machineAuditEvent = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
