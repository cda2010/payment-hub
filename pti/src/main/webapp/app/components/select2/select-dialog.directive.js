(function() {
    "use strict";
    angular.module('ptiApp').directive('jSelect2Dialog', select2);
    select2.$inject = ['$uibModal', "$rootScope"];

    function select2($uibModal, $rootScope) {
        var modalInstance;
        return {
            restrict: 'AE',
            scope: {
                datas: '=jList',
                model: '=jModel',
                multiple: '=jMultiple',
                columns: '=jColumns',
                eventName: '@jEventName',
                onFilter: '&jOnSearch'
            },
            link: function(scope, iElement, iAttrs, iCtrl) {
                var d = {
                    templateUrl: 'app/components/select2/select-dialog.html',
                    controller: function() {
                        var ctrl = this;
                        ctrl.query = "";
                        ctrl.cancel = cancel;
                        ctrl.confirm = confirm;
                        ctrl.isChecked = isChecked;
                        ctrl.toggle = toggle;
                        ctrl.datas = scope.datas;
                        ctrl.columns = scope.columns;
                        ctrl.onFilter = scope.onFilter;
                        ctrl.check = check;
                        ctrl.uncheck = uncheck;
                        ctrl.multiple = scope.multiple;
                        ctrl.toggleAll = toggleAll;
                        ctrl.getColumnTitle = getColumnTitle;
                        ctrl.getColumnName = getColumnName;
                        ctrl.checkAll=checkAll;

                        ctrl.search=function search() {
                            ctrl.initModel();
                            $rootScope.$broadcast(scope.eventName, ctrl.query);
                            console.log('find',ctrl.query,ctrl.datas);
                        };
                        console.log('all',scope.columns);
                        console.log('data***----',ctrl.datas);
                        console.log('model***----',ctrl);

                       ctrl.initModel= function initModel() {
                            ctrl.model = angular.copy(scope.model);
                            if (angular.isUndefined(ctrl.model)) {
                                if (ctrl.multiple) {
                                    ctrl.model = [];
                                } else {
                                    ctrl.model = '';
                                }
                            }
                        }

                        scope.$watch(angular.bind(scope, function() {
                            return scope.datas;
                        }), function(items) {
                            ctrl.datas = items;
                        });

                        /**A SUIVRE : L'appel de la fonction ctrl.onFilter ne fonctionne pas ! Le
                         * newValue passé est toujours vide !
                         */
                        scope.$watch(angular.bind(ctrl, function() {
                            return ctrl.query;
                        }), function(newValue) {
                            scope.filterText = newValue;
                        });

                        ctrl.model = angular.copy(scope.model);
                        if (angular.isUndefined(ctrl.model)) {
                            if (ctrl.multiple) {
                                ctrl.model = [];
                            } else {
                                ctrl.model = '';
                            }
                        }

                        function getColumnTitle(column) {
                            console.log('colum',column);
                            if (angular.isDefined(column) && column !== null) {
                                if (column.indexOf(',') !== -1) {
                                    var items = column.split(',');
                                    if (items.length !== 2) {
                                        console.error("***Column " + column + " is incorrect");
                                    }
                                    return items[1].trim();
                                } else {
                                    return column.trim();
                                }
                            }
                            return "";
                        }

                        function getColumnName(column) {
                            console.log('colum name',column);
                            if (angular.isDefined(column) && column !== null) {
                                if (column.indexOf(',') !== -1) {
                                    var items = column.split(',');
                                    if (items.length !== 2) {
                                        console.error("***Column " + column + " is incorrect");
                                    }
                                    return items[0];
                                } else {
                                    return column;
                                }
                            } else {
                                console.error("***Column name can't be null");
                                return "";
                            }
                        }

                        function cancel() {
                            modalInstance.dismiss('cancel');
                        }

                        function confirm() {
                            modalInstance.close(ctrl.model);
                        }

                        function isChecked(item) {
                            if (ctrl.multiple) {
                                return _.includes(ctrl.model, item.id);
                            } else {
                                return ctrl.model === item.id;
                            }
                        }

                        function toggle(item) {
                            if (ctrl.isChecked(item)) {
                                ctrl.uncheck(item);
                            } else {
                                console.log('ctrl.check',ctrl.check);
                                ctrl.check(item);
                            }
                        }

                        function toggleAll() {
                            if (ctrl.allSelected) {
                                angular.forEach(ctrl.datas, function(val, key) {
                                    ctrl.check(val);
                                });
                            } else {
                                angular.forEach(ctrl.datas, function(val, key) {
                                    ctrl.uncheck(val);
                                });
                            }
                        }
                        function checkAll() {
                            console.log('Selected All');
                            angular.forEach(ctrl.datas, function(val, key) {
                                ctrl.check(val);
                            });
                        }
                        function check(item) {
                            if (ctrl.multiple) {
                                console.log('ctrl.model',ctrl.model);
                                ctrl.model.push(item.id);
                            } else {
                                ctrl.model = item.id;
                            }
                        }

                        function uncheck(item) {
                            if (ctrl.multiple) {
                                var index=ctrl.model.indexOf(item.id);
                               ctrl.model.splice(index, 1);
                            } else {
                                ctrl.model = '';
                            }
                        }
                    },
                    controllerAs: 'ctrl',
                    size: 'md',
                    backdrop: 'static'
                };

                iElement.on("click", function(event) {
                    if (angular.isDefined(scope.eventName)) {
                        $rootScope.$broadcast(scope.eventName);
                    }
                    modalInstance = $uibModal.open(d);
                    modalInstance.result.then(
                        function(res) {
                            scope.model = res;
                        },
                        function() {

                        });
                });
            }
        };
    }
})();
