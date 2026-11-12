(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDiffusionDetailController', ListeDiffusionDetailController);

    ListeDiffusionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ListeDiffusion', 'DetailListeDiffusion'];

    function ListeDiffusionDetailController($scope, $rootScope, $stateParams, previousState, entity, ListeDiffusion, DetailListeDiffusion) {
        var vm = this;

        vm.listeDiffusion = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:listeDiffusionUpdate', function(event, result) {
            vm.listeDiffusion = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
