(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FaqDetailController', FaqDetailController);

    FaqDetailController.$inject = ['$scope', '$rootScope', '$stateParams','$uibModalInstance' ,'DataUtils', 'entity', 'Faq'];

    function FaqDetailController($scope, $rootScope, $stateParams,  $uibModalInstance,DataUtils, entity, Faq) {
        var vm = this;

        vm.faq = entity;
        //vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.clear=clear;

        var unsubscribe = $rootScope.$on('ptiApp:faqUpdate', function(event, result) {
            vm.faq = result;
        });
        $scope.$on('$destroy', unsubscribe);

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
