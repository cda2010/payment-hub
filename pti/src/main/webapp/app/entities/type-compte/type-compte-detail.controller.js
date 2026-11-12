(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeCompteDetailController', TypeCompteDetailController);

    TypeCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeCompte', 'Compte'];

    function TypeCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeCompte, Compte) {
        var vm = this;

        vm.typeCompte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typeCompteUpdate', function(event, result) {
            vm.typeCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
