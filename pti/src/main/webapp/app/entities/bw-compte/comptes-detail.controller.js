(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComptesDetailController', ComptesDetailController);

    ComptesDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Comptes'];

    function ComptesDetailController($scope, $rootScope, $stateParams, previousState, entity, Comptes) {
        var vm = this;

        vm.comptes = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('comptes', function(event, result) {
            vm.comptes = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
