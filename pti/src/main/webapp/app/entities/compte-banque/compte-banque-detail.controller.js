(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteBanqueDetailController', CompteBanqueDetailController);

    CompteBanqueDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'CompteBanque', 'Devise'];

    function CompteBanqueDetailController($scope, $rootScope, $stateParams, previousState, entity, CompteBanque, Devise) {
        var vm = this;

        vm.compteBanque = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:compteBanqueUpdate', function(event, result) {
            vm.compteBanque = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
