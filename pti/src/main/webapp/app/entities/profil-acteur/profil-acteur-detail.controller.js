(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurDetailController', ProfilActeurDetailController);

    ProfilActeurDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ProfilActeur', 'TableauFrais', 'UniteOrganisation', 'Client', 'TableauCumul', 'TableauRepartitionCommission'];

    function ProfilActeurDetailController($scope, $rootScope, $stateParams, previousState, entity, ProfilActeur, TableauFrais, UniteOrganisation, Client, TableauCumul, TableauRepartitionCommission) {
        var vm = this;

        vm.profilActeur = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:profilActeurUpdate', function(event, result) {
            vm.profilActeur = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
