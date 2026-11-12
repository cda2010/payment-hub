(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneRepartitionCommissionDetailController', LigneRepartitionCommissionDetailController);

    LigneRepartitionCommissionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneRepartitionCommission', 'TableauRepartitionCommission'];

    function LigneRepartitionCommissionDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneRepartitionCommission, TableauRepartitionCommission) {
        var vm = this;

        vm.ligneRepartitionCommission = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneRepartitionCommissionUpdate', function(event, result) {
            vm.ligneRepartitionCommission = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
