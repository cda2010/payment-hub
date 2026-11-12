(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionFraisDetailController', RepartitionFraisDetailController);

    RepartitionFraisDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RepartitionFrais', 'TypeService'];

    function RepartitionFraisDetailController($scope, $rootScope, $stateParams, previousState, entity, RepartitionFrais, TypeService) {
        var vm = this;

        vm.repartitionFrais = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:repartitionFraisUpdate', function(event, result) {
            vm.repartitionFrais = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
