(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientCreaDetailController', ClientCreaDetailController);

    ClientCreaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Client', 'Personne', 'Compte', 'TableauFrais'];

    function ClientCreaDetailController($scope, $rootScope, $stateParams, previousState, entity, Client, Personne, Compte, TableauFrais) {
        var vm = this;

        vm.client = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:clientUpdate', function(event, result) {
            vm.client = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
