(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceDetailController', ServiceDetailController);

    ServiceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Service', 'FournisseurService', 'InterfaceUtilisateur', 'Methode', 'TypeService'];

    function ServiceDetailController($scope, $rootScope, $stateParams, previousState, entity, Service, FournisseurService, InterfaceUtilisateur, Methode, TypeService) {
        var vm = this;

        vm.service = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:serviceUpdate', function(event, result) {
            vm.service = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
