(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('InterfaceUtilisateurDetailController', InterfaceUtilisateurDetailController);

    InterfaceUtilisateurDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'InterfaceUtilisateur', 'ServiceTraitement', 'ParametreUtiliateur'];

    function InterfaceUtilisateurDetailController($scope, $rootScope, $stateParams, previousState, entity, InterfaceUtilisateur, ServiceTraitement, ParametreUtiliateur) {
        var vm = this;

        vm.interfaceUtilisateur = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('exempleApp:interfaceUtilisateurUpdate', function(event, result) {
            vm.interfaceUtilisateur = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
