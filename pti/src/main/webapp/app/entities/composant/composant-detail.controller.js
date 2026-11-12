(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComposantDetailController', ComposantDetailController);

    ComposantDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState','Champs'];

    function ComposantDetailController($scope, $rootScope, $stateParams, previousState, entity,Champs) {
        var vm = this;

        vm.composant = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:composantUpdate', function(event, result) {
            vm.composant = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
