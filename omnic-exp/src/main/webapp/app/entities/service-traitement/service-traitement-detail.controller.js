(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ServiceTraitementDetailController', ServiceTraitementDetailController);

    ServiceTraitementDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ServiceTraitement', 'FournisseurService', 'InterfaceUtilisateur', 'Methode', 'TypeService'];

    function ServiceTraitementDetailController($scope, $rootScope, $stateParams, previousState, entity, ServiceTraitement, FournisseurService, InterfaceUtilisateur, Methode, TypeService) {
        var vm = this;

        vm.serviceTraitement = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:serviceTraitementUpdate', function(event, result) {
            vm.serviceTraitement = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
