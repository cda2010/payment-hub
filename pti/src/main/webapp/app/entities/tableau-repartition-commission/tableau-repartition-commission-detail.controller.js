(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauRepartitionCommissionDetailController', TableauRepartitionCommissionDetailController);

    TableauRepartitionCommissionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TableauRepartitionCommission', 'LigneRepartitionCommission', 'TypeService'];

    function TableauRepartitionCommissionDetailController($scope, $rootScope, $stateParams, previousState, entity, TableauRepartitionCommission, LigneRepartitionCommission, TypeService) {
        var vm = this;

        vm.tableauRepartitionCommission = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:tableauRepartitionCommissionUpdate', function(event, result) {
            vm.tableauRepartitionCommission = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
