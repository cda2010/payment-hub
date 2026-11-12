(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalDetailController', CanalDetailController);

    CanalDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Canal', 'Operation', 'TableauFrais', 'TypeService'];

    function CanalDetailController($scope, $rootScope, $stateParams, previousState, entity, Canal, Operation, TableauFrais, TypeService) {
        var vm = this;

        vm.canal = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:canalUpdate', function(event, result) {
            vm.canal = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
