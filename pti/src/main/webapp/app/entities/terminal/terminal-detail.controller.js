(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TerminalDetailController', TerminalDetailController);

    TerminalDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Terminal'];

    function TerminalDetailController($scope, $rootScope, $stateParams, previousState, entity, Terminal) {
        var vm = this;

        vm.terminal = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:terminalUpdate', function(event, result) {
            vm.terminal = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
