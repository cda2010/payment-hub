(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementTransfertDetailController', LigneChargementTransfertDetailController);

    LigneChargementTransfertDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneChargementTransfert'];

    function LigneChargementTransfertDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneChargementTransfert) {
        var vm = this;

        vm.ligneChargementTransfert = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneChargementTransfertUpdate', function(event, result) {
            vm.ligneChargementTransfert = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
