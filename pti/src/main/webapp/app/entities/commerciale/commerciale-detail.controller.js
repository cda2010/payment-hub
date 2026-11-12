(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommercialeDetailController', CommercialeDetailController);

    CommercialeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Commerciale'];

    function CommercialeDetailController($scope, $rootScope, $stateParams, previousState, entity, Commerciale) {
        var vm = this;

        vm.commerciale = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:commercialeUpdate', function(event, result) {
            vm.commerciale = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
