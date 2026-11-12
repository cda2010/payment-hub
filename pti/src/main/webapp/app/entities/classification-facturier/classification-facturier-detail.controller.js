(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClassificationFacturierDetailController', ClassificationFacturierDetailController);

    ClassificationFacturierDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ClassificationFacturier'];

    function ClassificationFacturierDetailController($scope, $rootScope, $stateParams, previousState, entity, ClassificationFacturier) {
        var vm = this;

        vm.classificationFacturier = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:classificationFacturierUpdate', function(event, result) {
            vm.classificationFacturier = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
