(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientDetailController', ClientDetailController);

    ClientDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Client', 'Personne', 'Compte', 'TableauFrais'];

    function ClientDetailController($scope, $rootScope, $stateParams, previousState, entity, Client, Personne, Compte, TableauFrais) {
        var vm = this;

        vm.client = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:clientUpdate', function(event, result) {
            vm.client = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
