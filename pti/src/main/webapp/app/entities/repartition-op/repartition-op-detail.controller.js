(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionOpDetailController', RepartitionOpDetailController);

    RepartitionOpDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RepartitionOp', 'Operation'];

    function RepartitionOpDetailController($scope, $rootScope, $stateParams, previousState, entity, RepartitionOp, Operation) {
        var vm = this;

        vm.repartitionOp = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:repartitionOpUpdate', function(event, result) {
            vm.repartitionOp = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
