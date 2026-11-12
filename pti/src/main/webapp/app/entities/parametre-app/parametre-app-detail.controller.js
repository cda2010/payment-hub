(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParametreAppDetailController', ParametreAppDetailController);

    ParametreAppDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ParametreApp'];

    function ParametreAppDetailController($scope, $rootScope, $stateParams, previousState, entity, ParametreApp) {
        var vm = this;

        vm.parametreApp = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:parametreAppUpdate', function(event, result) {
            vm.parametreApp = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
