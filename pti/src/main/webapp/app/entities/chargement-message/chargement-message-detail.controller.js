(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementMessageDetailController', ChargementMessageDetailController);

    ChargementMessageDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ChargementMessage'];

    function ChargementMessageDetailController($scope, $rootScope, $stateParams, previousState, entity, ChargementMessage) {
        var vm = this;

        vm.chargementMessage = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:chargementMessageUpdate', function(event, result) {
            vm.chargementMessage = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
