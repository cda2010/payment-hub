(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TypeCompteDetailController', TypeCompteDetailController);

    TypeCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeCompte'];

    function TypeCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeCompte) {
        var vm = this;

        vm.typeCompte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:typeCompteUpdate', function(event, result) {
            vm.typeCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
