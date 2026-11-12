(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneMessageDetailController', LigneMessageDetailController);

    LigneMessageDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneMessage', 'ChargementMessage'];

    function LigneMessageDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneMessage, ChargementMessage) {
        var vm = this;

        vm.ligneMessage = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneMessageUpdate', function(event, result) {
            vm.ligneMessage = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
