(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EntityAuditEventDetailController', EntityAuditEventDetailController);

    EntityAuditEventDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EntityAuditEvent'];

    function EntityAuditEventDetailController($scope, $rootScope, $stateParams, previousState, entity, EntityAuditEvent) {
        var vm = this;

        vm.entityAuditEvent = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:entityAuditEventUpdate', function(event, result) {
            vm.entityAuditEvent = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
