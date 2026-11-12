(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementCompteDetailController', ChargementCompteDetailController);

    ChargementCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ChargementCompte'];

    function ChargementCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, ChargementCompte) {
        var vm = this;

        vm.chargementCompte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:chargementCompteUpdate', function(event, result) {
            vm.chargementCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
