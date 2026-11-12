(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementCompteDetailController', LigneChargementCompteDetailController);

    LigneChargementCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneChargementCompte'];

    function LigneChargementCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneChargementCompte) {
        var vm = this;

        vm.ligneChargementCompte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneChargementCompteUpdate', function(event, result) {
            vm.ligneChargementCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
