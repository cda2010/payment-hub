(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneReglementDetailController', LigneReglementDetailController);

    LigneReglementDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneReglement', 'Reglement', 'UniteOrganisation'];

    function LigneReglementDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneReglement, Reglement, UniteOrganisation) {
        var vm = this;

        vm.ligneReglement = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneReglementUpdate', function(event, result) {
            vm.ligneReglement = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
