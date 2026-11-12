(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailListeDiffusionDetailController', DetailListeDiffusionDetailController);

    DetailListeDiffusionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'DetailListeDiffusion', 'ListeDiffusion'];

    function DetailListeDiffusionDetailController($scope, $rootScope, $stateParams, previousState, entity, DetailListeDiffusion, ListeDiffusion) {
        var vm = this;

        vm.detailListeDiffusion = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:detailListeDiffusionUpdate', function(event, result) {
            vm.detailListeDiffusion = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
