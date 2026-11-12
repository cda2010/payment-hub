(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementTransfertDetailController', ChargementTransfertDetailController);

    ChargementTransfertDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ChargementTransfert'];

    function ChargementTransfertDetailController($scope, $rootScope, $stateParams, previousState, entity, ChargementTransfert) {
        var vm = this;

        vm.chargementTransfert = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:chargementTransfertUpdate', function(event, result) {
            vm.chargementTransfert = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
