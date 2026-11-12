(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartionFraisEmAcqDetailController', RepartionFraisEmAcqDetailController);

    RepartionFraisEmAcqDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RepartionFraisEmAcq', 'TypeService'];

    function RepartionFraisEmAcqDetailController($scope, $rootScope, $stateParams, previousState, entity, RepartionFraisEmAcq, TypeService) {
        var vm = this;

        vm.repartionFraisEmAcq = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:repartionFraisEmAcqUpdate', function(event, result) {
            vm.repartionFraisEmAcq = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
